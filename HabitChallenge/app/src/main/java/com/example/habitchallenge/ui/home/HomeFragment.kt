package com.example.habitchallenge.ui.home

import android.animation.ObjectAnimator
import android.content.ContentValues
import android.media.MediaCodec.LinearBlock
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitchallenge.AddHabitDialogFragment
import com.example.habitchallenge.HabitShareDialogFragment
import com.example.habitchallenge.R
import com.example.habitchallenge.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import utils.HabitAdapter
import utils.HabitData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.messaging.RemoteMessage

class HomeFragment : Fragment(), AddHabitDialogFragment.DialogNextButtonClickListener,
    HabitAdapter.HabitClicksInterface, HabitShareDialogFragment.DialogNextButtonClickListener {
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore;
    private lateinit var navController: NavController;
    private lateinit var binding: FragmentHomeBinding;
    private var addHabitFragment: AddHabitDialogFragment?= null;
    private lateinit var shareHabitFragment: HabitShareDialogFragment;
    private lateinit var adapter: HabitAdapter;
    private lateinit var habits: MutableList<HabitData>;
    private lateinit var users: List<Map<String, String>>;

    private var username: String? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        val habitsData = mutableListOf<HabitData>();
        setToken()
        users = getUserNames()
        processHabits()
        registerEvents()

        val userId = auth.currentUser?.uid;

        val usersCollection = db.collection("users")

        usersCollection.document(userId.toString()).get().addOnSuccessListener {document ->
            username = document.getString("username")
            if (username != null) {
                binding.username.text = "Привет, $username";
            } else {
                Log.d(ContentValues.TAG, "не найден username $username для $userId!")
            }
        }.addOnFailureListener { exception ->
            Log.d(ContentValues.TAG, "Ошибка получения имени $exception!")
        }
    }

    private fun setToken() {
        val userId = auth.currentUser?.uid;
        val usersCollection = db.collection("users")

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                // Обработка успешного получения токена
                println("Токен устройства (токен пользователя): $token")
                usersCollection.document(userId.toString())
                    .update("deviceToken", token)
                    .addOnSuccessListener {
                        println("Токен устройства успешно добавлен к пользователю с идентификатором $userId")
                    }
                    .addOnFailureListener { e ->
                        println("Ошибка при добавлении токена устройства к пользователю с идентификатором $userId: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                // Обработка ошибки при получении токена
                println("Ошибка при получении токена устройства: ${e.message}")
            }
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        navController = Navigation.findNavController(view)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        habits = mutableListOf()
        adapter = HabitAdapter(habits, auth.currentUser?.uid.toString(), context)
        adapter.setListener(this)
        binding.recyclerView.adapter = adapter;
    }

    private fun registerEvents() {
        binding.username.setOnClickListener{
            if (username != null) navController.navigate(R.id.action_navigation_home_to_profileFragment)
        }

        binding.addHabitButton.setOnClickListener{
            if (addHabitFragment != null)
                childFragmentManager.beginTransaction().remove(addHabitFragment!!).commit()

            addHabitFragment = AddHabitDialogFragment()
            addHabitFragment!!.setListener(this)
            addHabitFragment!!.show(
                childFragmentManager,
                "AddHabitDialogFragment"
            )
        }
    }

    fun getUserNames(): List<Map<String, String>> {
        val userIds = mutableListOf<Map<String, String>>()

        try {
            db.collection("users").get().addOnSuccessListener {usersCollection ->
                for (userDocument in usersCollection.documents) {
                    userIds.add(mapOf<String, String>("id" to userDocument.id, "username" to userDocument.get("username").toString()))
                }
            }

        } catch (e: Exception) {
            // Обработка ошибок при получении данных пользователей
        }

        return userIds
    }

    fun processHabits() {
        getHabits { habits ->
            val habitsData = mutableListOf<HabitData>()
            habits.forEach { habit ->
                val id = habit["id"].toString()
                val name = habit["name"].toString()
                val description = habit["description"].toString()
                val iconUrl = habit["iconUrl"].toString()
                val color = habit["color"].toString()

                // Извлечение и преобразование данных пользователей
                val usersMap = habit["users"] as? Map<String, Any> ?: emptyMap()
                val usersList = usersMap.map { (userId, userData) ->
                    val userDates = (userData as? Map<String, List<String>>)?.get("dates") ?: emptyList()
                    mapOf(userId to userDates)
                }

                val habitDataItem = HabitData(id, name, description, usersList)
                habitsData.add(habitDataItem)
            }
            updateHabits(habitsData)
            adapter.updateHabits(habitsData)
        }
    }

    override fun onSaveHabit(habit: String, description: String) {
        val users = mapOf(
            auth.currentUser?.uid to listOf<String>()
        )
        val habit = hashMapOf(
            "name" to habit,
            "description" to description,
            "users" to users
        )

        db.collection("Habits").add(habit).addOnSuccessListener {
            val habitsData = mutableListOf<HabitData>();

            processHabits()
            Toast.makeText(context, "Привычка добавлена успешно!", Toast.LENGTH_SHORT).show()
            addHabitFragment!!.dismiss()
        }.addOnFailureListener{ exception ->
            Log.d(ContentValues.TAG, "Ошибка добавления привычки $exception!")
        }
    }

    override fun onUpdateHabit(habitData: HabitData) {
        println(habitData)
        val habitRef = db.collection("Habits").document(habitData.id)
        val updates = hashMapOf<String, Any>(
            "name" to habitData.name,
            "description" to habitData.description
        )

        habitRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Привычка изменена успешно!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                println("Error updating habit: ${e.message}")
            }

        processHabits()
        addHabitFragment!!.dismiss()
    }

    fun getHabits(onComplete: (List<Map<String, Any>>) -> Unit) {
        val userId = auth.currentUser?.uid;

        db.collection("Habits")
            .get()
            .addOnSuccessListener { result ->
                val habitList = mutableListOf<Map<String, Any>>()

                for (document in result) {
                    val habit = document.data.toMutableMap();
                    habit["id"] = document.id;
                    val users = habit["users"] as? Map<*, *>
                    users?.let {
                        if (it.containsKey(userId)) {
                            habitList.add(habit)
                        }
                    }
                }

                onComplete(habitList)
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
                onComplete(emptyList())
            }
    }

    fun updateHabits(newHabits: List<HabitData>) {
        habits.clear()
        habits.addAll(newHabits)
        val habitsCount = adapter.getItemCount();
        val completedHabitsCount = adapter.getCompletedItemCount();

        binding.allHabitsCount.setText("Всего привычек: ${habitsCount}")
        binding.completedHabitsCount.setText("Выполнено сегодня: ${completedHabitsCount}")

        binding.progressBar.max = habitsCount
        ObjectAnimator.ofInt(binding.progressBar, "progress", completedHabitsCount).setDuration(0).start()
        if (habitsCount == completedHabitsCount) {
            binding.completeMessage.visibility = View.VISIBLE;
        }else {
            binding.completeMessage.visibility = View.GONE;
        }
    }

    override fun onEditHabitBtnClicked(habitData: HabitData) {
        if (addHabitFragment != null)
            childFragmentManager.beginTransaction().remove(addHabitFragment!!).commit()

        addHabitFragment = AddHabitDialogFragment.newInstance(habitData.id, habitData.name, habitData.description)
        addHabitFragment!!.setListener(this)
        addHabitFragment!!.show(childFragmentManager, AddHabitDialogFragment.TAG)
    }

    override fun onHabitClicked(habitData: HabitData) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val userId = auth.currentUser?.uid;
        println(habitData)

        val habitRef = db.collection("Habits").document(habitData.id)
        habitRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val habitData = document.data?.toMutableMap()
                val users = habitData?.get("users") as? MutableMap<String, Any>
                val user = users?.get(userId) as? MutableMap<String, Any>
                val dates = user?.get("dates") as? MutableList<String> ?: mutableListOf()

                // Если последняя дата - сегодняшняя, удаляем ее. Иначе добавляем сегодняшнюю дату.
                if (dates.isNotEmpty() && dates.last() == today) {
                    dates.removeAt(dates.size - 1)
                } else {
                    dates.add(today)
                }

                // Обновляем данные пользователя и привычки
                users?.put(userId.toString(), mutableMapOf("dates" to dates))
                habitData?.put("users", users)

                habitRef.set(habitData!!).addOnSuccessListener {
                    processHabits()
                    println("Дата успешно обновлена.")
                }.addOnFailureListener { e ->
                    println("Ошибка обновления даты: $e")
                }
            } else {
                println("Документ не найден.")
            }
        }.addOnFailureListener { e ->
            println("Ошибка чтения документа: $e")
        }
    }

    override fun onDeleteHabitBtnClicked(habitData: HabitData) {
        db.collection("habits").document(habitData.id)
            .delete()
            .addOnSuccessListener {
                processHabits()
                Toast.makeText(context, "Привычка удалена успешно!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                println("Error deleting document ${e}")
            }
    }

    override fun onSharedHabitClicked(habitData: HabitData) {
        println(habitData)
        shareHabitFragment = HabitShareDialogFragment.newInstance(habitData.id, habitData.name, habitData.description, users)
        shareHabitFragment.setListener(this)
        shareHabitFragment.show(childFragmentManager, HabitShareDialogFragment.TAG)
    }

    override fun onShareHabit(habitData: HabitData, userId: String) {
        println(habitData);
        println(userId);
        val habitRef = FirebaseFirestore.getInstance().collection("Habits").document(habitData.id)
        val newUser = hashMapOf(
            "dates" to emptyList<String>()
        )
        habitRef.update("users.$userId", newUser)
            .addOnSuccessListener {
                Toast.makeText(context, "Пользователь успешно добавлен к привычке!", Toast.LENGTH_SHORT).show()
                val usersCollection = FirebaseFirestore.getInstance().collection("users")
                usersCollection.document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // Получаем значение свойства deviceToken из документа пользователя
                            val deviceToken = document.getString("deviceToken")
                            println("Токен устройства пользователя с идентификатором $userId: $deviceToken")
                        } else {
                            println("Документ пользователя с идентификатором $userId не найден")
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Ошибка при получении токена устройства пользователя с идентификатором $userId: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                println("Ошибка при добавлении пользователя к привычке ${e}")
            }
        shareHabitFragment.dismiss();
    }

    fun sendNotificationToUser(token: String, title: String, body: String) {
        val message = RemoteMessage.Builder(token)
            .setMessageId(java.util.UUID.randomUUID().toString())
            .setData(mapOf(
                "title" to title,
                "body" to body
            ))
            .build()
    }
}
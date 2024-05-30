package com.example.habitchallenge

import android.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitchallenge.databinding.FragmentHabitShareDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import utils.HabitAdapter
import utils.HabitData

data class UserItem(val id: String, val username: String) {
    override fun toString(): String {
        return username
    }
}

class HabitShareDialogFragment : DialogFragment() {
    private lateinit var db: FirebaseFirestore;
    private lateinit var binding: FragmentHabitShareDialogBinding;
    private lateinit var listener: DialogNextButtonClickListener;
    private var habitData: HabitData? = null
    private var users: MutableList<Map<String, String>> = mutableListOf()
    private val usersAdapter: ArrayAdapter<UserItem> by lazy {
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item)
    }

    companion object {
        const val TAG = "ShareHabitDialogFragment"

        @JvmStatic
        fun newInstance(id: String, name: String, description: String, users: List<Map<String, String>>) = HabitShareDialogFragment().apply {
            arguments = Bundle().apply {
                putString("id", id)
                putString("name", name)
                putString("description", description)
                putSerializable("users", users.toTypedArray())
            }
        }
    }

    fun setListener(listener: DialogNextButtonClickListener) {
        this.listener = listener;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHabitShareDialogBinding.inflate(inflater, container, false);
        binding.usersSpinner.adapter = usersAdapter;
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        getUserNames()

        if(arguments != null) {
            habitData = HabitData(arguments?.getString("id").toString(),
                arguments?.getString("name").toString(),
                arguments?.getString("description").toString(), listOf(mapOf())
            )
        }
        registerEvents()
    }

    private fun init(view: View) {
        db = FirebaseFirestore.getInstance()
    }

    private fun getUserNames() {
            db.collection("users").get().addOnSuccessListener {usersCollection ->
                for (userDocument in usersCollection.documents) {
                    users.add(mapOf<String, String>("id" to userDocument.id, "username" to userDocument.get("username").toString()))
                    println(users)
                }
                //binding.usersSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, arrayOf("123", "333"))
                updateUsers(users)
            }
    }

    fun updateUsers(users: MutableList<Map<String, String>>) {
        usersAdapter.clear()
        for (user in users) {
            val id = user["id"] ?: ""
            val username = user["username"] ?: ""
            usersAdapter.add(UserItem(id, username))
        }
    }

    private fun registerEvents() {
        binding.shareButton.setOnClickListener{
            val selectedPosition = binding.usersSpinner.selectedItemPosition

            val selectedUserItem = binding.usersSpinner.adapter.getItem(selectedPosition) as? UserItem

            if (selectedUserItem != null) {
                val userId = selectedUserItem.id

                listener.onShareHabit(habitData!!, userId)
            } else {
                Toast.makeText(context, "Выберите пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface DialogNextButtonClickListener {
        fun onShareHabit(habitData: HabitData, userId: String)
    }
}
package utils

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.habitchallenge.R
import com.example.habitchallenge.databinding.HabitItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitAdapter(private val habits: MutableList<HabitData>, private val userId: String, private val context: Context?): RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {
    private var listener: HabitClicksInterface? = null
    fun setListener(listener: HabitClicksInterface) {
        this.listener = listener;
    }

    class HabitViewHolder(val binding: HabitItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = HabitItemBinding.inflate(LayoutInflater.from(parent.context), parent, false);
        return HabitViewHolder(binding);
    }

    override fun getItemCount(): Int {
        return habits.size;
    }

    fun getCompletedItemCount(): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var completedCount = 0

        for (habit in habits) {
            val userDates = habit.users.find { it.containsKey(userId) }?.get(userId)
            if (userDates != null && userDates.isNotEmpty() && userDates.last() == today) {
                completedCount++
            }
        }

        return completedCount
    }

    fun updateHabits(newHabits: List<HabitData>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        with(holder) {
            with(habits[position]) {
                binding.habitName.text = this.name;

                // Получаем текущую дату
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = dateFormat.format(Date())

                // Найти элемент пользователя по userId
                val userDates = this.users.find { userMap ->
                    userMap.containsKey(userId)
                }?.get(userId)

                // Проверяем, совпадает ли последняя дата текущего пользователя с сегодняшней
                val lastDate = userDates?.lastOrNull()

                if (lastDate == today) {
                    binding.habitItem.background = ContextCompat.getDrawable(context!!, R.drawable.habit_item_success)
                } else {
                    binding.habitItem.background = ContextCompat.getDrawable(context!!, R.drawable.habit_item)
                }


                binding.editHabit.setOnClickListener{
                    listener?.onEditHabitBtnClicked(this)
                }

                binding.shareHabit.setOnClickListener{
                    listener?.onSharedHabitClicked(this)
                }

                binding.habitItem.setOnClickListener{
                    listener?.onHabitClicked(this)
                }

                binding.deleteHabit.setOnClickListener{
                    listener?.onDeleteHabitBtnClicked(this)
                }
            }
        }
    }

    interface HabitClicksInterface {
        fun onEditHabitBtnClicked(habitData: HabitData)
        fun onHabitClicked(habitData: HabitData)
        fun onSharedHabitClicked(habitData: HabitData)
        fun onDeleteHabitBtnClicked(habitData: HabitData)
    }
}
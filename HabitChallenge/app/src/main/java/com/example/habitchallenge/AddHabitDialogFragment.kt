package com.example.habitchallenge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.habitchallenge.databinding.FragmentAddHabitDialogBinding
import com.example.habitchallenge.ui.home.HomeFragment
import utils.HabitData

class AddHabitDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentAddHabitDialogBinding;
    private lateinit var listener: DialogNextButtonClickListener;
    private var habitData: HabitData? = null

    companion object {
        const val TAG = "AddHabitDialogFragment"

        @JvmStatic
        fun newInstance(id: String, name: String, description: String) = AddHabitDialogFragment().apply {
            arguments = Bundle().apply {
                putString("id", id)
                putString("name", name)
                putString("description", description)

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
        binding = FragmentAddHabitDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null) {
            habitData = HabitData(arguments?.getString("id").toString(),
                arguments?.getString("name").toString(),
                arguments?.getString("description").toString(), listOf(mapOf())
            )

            binding.nameInput.setText(habitData?.name)
            binding.descriptionInput.setText(habitData?.description)
        }
        registerEvents()
    }

    private fun registerEvents() {
        binding.createButton.setOnClickListener{
            val name = binding.nameInput.text.toString().trim()
            val description = binding.descriptionInput.text.toString().trim()

            if (name.isNotEmpty()) {
                if (habitData == null) {
                    listener.onSaveHabit(name, description)
                }else {
                    habitData?.name = name;
                    habitData?.description = description;
                    listener.onUpdateHabit(habitData!!)
                }
            }
            else {
                Toast.makeText(context, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface DialogNextButtonClickListener {
        fun onSaveHabit(habit: String, description: String)
        fun onUpdateHabit(habitData: HabitData)
    }
}
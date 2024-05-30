package utils

data class HabitData(val id: String, var name: String,
                     var description: String,
                     val users: List<Map<String, List<String>>>)

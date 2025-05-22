package com.example.examen2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox // Nuevo import
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val userList: MutableList<User>,
    private val clickListener: (User) -> Unit // Para editar usuario
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    val selectedUserUids = mutableSetOf<String>() // Conjunto para almacenar UIDs de usuarios seleccionados

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserNameItem)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmailItem)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRoleItem)
        val checkboxUserSelect: CheckBox = itemView.findViewById(R.id.checkboxUserSelect) // Nuevo
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.tvUserName.text = "Nombre: ${currentUser.name}"
        holder.tvUserEmail.text = "Email: ${currentUser.email}"
        holder.tvUserRole.text = "Rol: ${currentUser.role}"

        // Configurar el CheckBox
        holder.checkboxUserSelect.isChecked = selectedUserUids.contains(currentUser.uid)
        holder.checkboxUserSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedUserUids.add(currentUser.uid)
            } else {
                selectedUserUids.remove(currentUser.uid)
            }
        }

        // El clic en el elemento completo sigue siendo para editar
        holder.itemView.setOnClickListener {
            clickListener(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    fun updateUsers(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun getSelectedUids(): List<String> {
        return selectedUserUids.toList()
    }

    fun clearSelections() {
        selectedUserUids.clear()
        notifyDataSetChanged()
    }
}
package com.example.examen2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: MutableList<User>, private val clickListener: (User) -> Unit) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserNameItem)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmailItem)
        val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRoleItem)
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

        holder.itemView.setOnClickListener {
            clickListener(currentUser)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    // Método para actualizar la lista de usuarios (útil si hay cambios en Firestore)
    fun updateUsers(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        notifyDataSetChanged()
    }
}
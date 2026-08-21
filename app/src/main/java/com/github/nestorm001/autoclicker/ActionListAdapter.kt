package com.github.nestorm001.autoclicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.nestorm001.autoclicker.bean.ActionItem

/**
 * Adapter cho RecyclerView hiển thị danh sách kịch bản
 */
class ActionListAdapter(
    private val actions: MutableList<ActionItem>,
    private val onDetailClick: (ActionItem) -> Unit,
    private val onExportClick: (ActionItem) -> Unit,
    private val onRunClick: (ActionItem) -> Unit,
    private val onActiveToggle: (ActionItem) -> Unit
) : RecyclerView.Adapter<ActionListAdapter.ActionViewHolder>() {

    inner class ActionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIndex: TextView = itemView.findViewById(R.id.tvIndex)
        val tvActionName: TextView = itemView.findViewById(R.id.tvActionName)
        val btnActive: Button = itemView.findViewById(R.id.btnActive)
        val btnDetail: Button = itemView.findViewById(R.id.btnDetail)
        val btnExport: Button = itemView.findViewById(R.id.btnExport)
        val btnRun: Button = itemView.findViewById(R.id.btnRun)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_action, parent, false)
        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val action = actions[position]
        
        // Hiển thị STT và tên
        holder.tvIndex.text = (position + 1).toString()
        holder.tvActionName.text = action.name
        
        // Cập nhật trạng thái button Active
        holder.btnActive.text = if (action.isActive) {
            holder.itemView.context.getString(R.string.active)
        } else {
            holder.itemView.context.getString(R.string.inactive)
        }
        
        // Xử lý click Active button
        holder.btnActive.setOnClickListener {
            action.toggleActive()
            notifyItemChanged(position)
            onActiveToggle(action)
        }
        
        // Xử lý click Detail button
        holder.btnDetail.setOnClickListener {
            onDetailClick(action)
        }
        
        // Xử lý click Export button
        holder.btnExport.setOnClickListener {
            onExportClick(action)
        }
        
        // Xử lý click Run button
        holder.btnRun.setOnClickListener {
            onRunClick(action)
        }
    }

    override fun getItemCount(): Int = actions.size
    
    // Thêm action vào danh sách
    fun addAction(action: ActionItem) {
        actions.add(action)
        notifyItemInserted(actions.size - 1)
    }
    
    // Xóa action khỏi danh sách
    fun removeAction(position: Int) {
        actions.removeAt(position)
        notifyItemRemoved(position)
    }
}

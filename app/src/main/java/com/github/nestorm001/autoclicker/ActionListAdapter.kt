package com.github.nestorm001.autoclicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.nestorm001.autoclicker.bean.Action

class ActionListAdapter(
    private val actions: List<Action>,
    private val selectedActions: MutableSet<Long>,
    private val isSelectionMode: () -> Boolean,
    private val onDetailClick: (Action) -> Unit,
    private val onExportClick: (Action) -> Unit,
    private val onRunClick: (Action) -> Unit,
    private val onLongClick: (Action) -> Unit,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ActionListAdapter.ActionViewHolder>() {

    class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvActionName: TextView = view.findViewById(R.id.tvActionName)
        val tvActionInfo: TextView = view.findViewById(R.id.tvActionInfo)
        val checkboxSelect: CheckBox = view.findViewById(R.id.checkboxSelect)
        val btnDetail: ImageButton = view.findViewById(R.id.btnDetail)
        val btnExport: ImageButton = view.findViewById(R.id.btnExport)
        val btnRun: ImageButton = view.findViewById(R.id.btnRun)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_action, parent, false)
        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val action = actions[position]
        
        holder.tvActionName.text = action.name
        holder.tvActionInfo.text = "${action.clickPoints.size} điểm - ${action.repeatCount} lần"
        
        // Show/hide checkbox based on selection mode
        if (isSelectionMode()) {
            holder.checkboxSelect.visibility = View.VISIBLE
            holder.checkboxSelect.isChecked = selectedActions.contains(action.id)
            
            holder.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedActions.add(action.id)
                } else {
                    selectedActions.remove(action.id)
                }
                onSelectionChanged()
            }
            
            holder.itemView.setOnClickListener {
                holder.checkboxSelect.isChecked = !holder.checkboxSelect.isChecked
            }
        } else {
            holder.checkboxSelect.visibility = View.GONE
            holder.checkboxSelect.setOnCheckedChangeListener(null)
            holder.itemView.setOnClickListener(null)
        }
        
        // Long click to enter selection mode
        holder.itemView.setOnLongClickListener {
            onLongClick(action)
            true
        }
        
        holder.btnDetail.setOnClickListener {
            onDetailClick(action)
        }
        
        holder.btnExport.setOnClickListener {
            onExportClick(action)
        }
        
        holder.btnRun.setOnClickListener {
            onRunClick(action)
        }
    }

    override fun getItemCount() = actions.size
}

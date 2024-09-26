package st10036509.countify.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.model.CounterModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CounterAdapter(private val counterList: List<CounterModel>) :
    RecyclerView.Adapter<CounterAdapter.CounterViewHolder>() {

    class CounterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tv_itemName)
        val tvCounter: TextView = itemView.findViewById(R.id.tv_counter)
        val ivPlusButton: ImageView = itemView.findViewById(R.id.iv_plusButton)
        val ivMinusButton: ImageView = itemView.findViewById(R.id.iv_minusButton)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_counter, parent, false)
        return CounterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CounterViewHolder, position: Int) {
        val currentCounter = counterList[position]
        val formattedDate = formatTimestampToDate(currentCounter.createdTimestamp)

        // Set UI values
        holder.tvItemName.text = currentCounter.name.trim()
        holder.tvCounter.text = currentCounter.currentValue.toString().trim()
        holder.tvDate.text = formattedDate.trim()

        // Increment counter
        holder.ivPlusButton.setOnClickListener {
            // increment the current counter value by its increment value
            val updatedValue = currentCounter.currentValue + currentCounter.changeValue
            currentCounter.currentValue = updatedValue  // update the local object
            holder.tvCounter.text = updatedValue.toString()  // Update UI with new value

            // save the updated value to Firestore
            saveUpdatedCounterValueToFirestore(currentCounter)
        }

        // Decrement counter
        holder.ivMinusButton.setOnClickListener {
            // Decrement the current counter value by its decrement value
            val updatedValue = currentCounter.currentValue - currentCounter.changeValue
            currentCounter.currentValue = updatedValue
            holder.tvCounter.text = updatedValue.toString()

            // save updated value to firestore
            saveUpdatedCounterValueToFirestore(currentCounter)
        }
    }

    /**
     * Save the updated counter value to Firestore
     */
    private fun saveUpdatedCounterValueToFirestore(counter: CounterModel) {
        val counterDocumentRef = FirebaseFirestore.getInstance()
            .collection("counters_tests")
            .document(counter.counterId)

        // Update the current value in Firestore
        counterDocumentRef.update("currentValue", counter.currentValue)
            .addOnSuccessListener {
                // Successfully updated Firestore
                Log.d("Firestore", "Counter updated successfully")
            }
            .addOnFailureListener { e ->
                // Failed to update Firestore
                Log.w("Firestore", "Error updating counter", e)
            }
    }


    fun formatTimestampToDate(timestampInMillis: Long): String {
        val date = Date(timestampInMillis) //convert timestamp to date
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) //setting the required format
        return sdf.format(date) //returning formatted date
    }

    override fun getItemCount() = counterList.size
}
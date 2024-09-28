package st10036509.countify.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import st10036509.countify.R
import st10036509.countify.model.CounterModel
import st10036509.countify.service.Toaster
import st10036509.countify.user_interface.counter.CounterViewFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CounterAdapter(private val counterList: List<CounterModel>, private val fragment: Fragment) :
    RecyclerView.Adapter<CounterAdapter.CounterViewHolder>() {

        private var toaster: Toaster
    init {
        toaster = Toaster(fragment)
    }

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
        holder.tvCounter.text = currentCounter.count.toString().trim()
        holder.tvDate.text = formattedDate.trim()

        // Increment counter
        holder.ivPlusButton.setOnClickListener {
            // increment the current counter value by its increment value
            val updatedValue = currentCounter.count + currentCounter.changeValue
            currentCounter.count = updatedValue  // update the local object
            holder.tvCounter.text = updatedValue.toString()  // Update UI with new value

            // save the updated value to Firestore
            saveUpdatedCounterValueToFirestore(currentCounter)
        }

        // Decrement counter
        holder.ivMinusButton.setOnClickListener {
            // Decrement the current counter value by its decrement value
            val updatedValue = currentCounter.count - currentCounter.changeValue
            currentCounter.count = updatedValue
            holder.tvCounter.text = updatedValue.toString()

            // save updated value to firestore
            saveUpdatedCounterValueToFirestore(currentCounter)
        }
    }


    private fun saveUpdatedCounterValueToFirestore(counter: CounterModel) {
        val counterDocumentRef = counter.counterId?.let {
            FirebaseFirestore.getInstance()
                .collection("counters_tests")
                .document(it)
        }

        counterDocumentRef?.update("currentValue", counter.count)?.addOnSuccessListener {
            // Successfully updated Firestore, using getString from fragment context
            toaster.showToast(fragment.getString(R.string.counter_update_successful))
        }?.addOnFailureListener { e ->
            // Failed to update Firestore
            toaster.showToast(fragment.getString((R.string.counter_update_failed)))
        }
    }


    fun formatTimestampToDate(timestampInMillis: Long): String {
        val date = Date(timestampInMillis) //convert timestamp to date
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) //setting the required format
        return sdf.format(date) //returning formatted date
    }

    override fun getItemCount() = counterList.size
}
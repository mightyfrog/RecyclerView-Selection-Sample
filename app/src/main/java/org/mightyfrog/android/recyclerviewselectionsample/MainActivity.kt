package org.mightyfrog.android.recyclerviewselectionsample

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.selection.*
import androidx.recyclerview.widget.RecyclerView
import org.mightyfrog.android.recyclerviewselectionsample.databinding.ActivityMainBinding
import org.mightyfrog.android.recyclerviewselectionsample.databinding.ListItemBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val testData = mutableListOf<MyItem>()

    private lateinit var tracker: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTestData()

        ActivityMainBinding.inflate(layoutInflater).apply {
            recyclerView.adapter = MyItemAdapter()
            setContentView(root)
        }
    }

    override fun onBackPressed() {
        if (tracker.selection.isEmpty) {
            super.onBackPressed()
        } else {
            tracker.clearSelection()
        }
    }

    private fun initTestData() {
        val loremIpsum = getString(R.string.lorem_ipsum)
        for (i in 0 until 100) {
            testData.add(
                MyItem(
                    i.toLong(),
                    loremIpsum.substring(0, Random.Default.nextInt(loremIpsum.length))
                )
            )
        }
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {

        @Nullable
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view: View? = recyclerView.findChildViewUnder(e.x, e.y)
            if (view != null) {
                val holder = recyclerView.getChildViewHolder(view)
                if (holder is MyItemViewHolder) {
                    return holder.getItemDetails()
                }
            }

            return null
        }
    }

    inner class MyItemAdapter : RecyclerView.Adapter<MyItemViewHolder>() {

        init {
            setHasStableIds(true) // don't forget this!
        }

        override fun getItemCount(): Int = 100

        override fun getItemId(position: Int): Long = position.toLong()

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            tracker = SelectionTracker.Builder(
                "my-selection-id",
                recyclerView,
                StableIdKeyProvider(recyclerView),
                MyItemDetailsLookup(recyclerView),
                StorageStrategy.createLongStorage()
            ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
            ).build()

            tracker.addObserver(
                object : SelectionTracker.SelectionObserver<Long>() {

                    override fun onSelectionChanged() {
                        val size: Int = tracker.selection.size()
                        android.util.Log.e(javaClass.simpleName, "$size items selected")
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyItemViewHolder {
            val binding = ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return MyItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: MyItemViewHolder, position: Int) {
            holder.bind(position, tracker.isSelected(position.toLong()))
        }
    }

    inner class MyItemViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, selected: Boolean) {
            binding.item = testData[position]
            binding.executePendingBindings()

            if (selected) {
                binding.textView.setTextColor(Color.MAGENTA)
            } else {
                binding.textView.setTextColor(Color.WHITE)
            }
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> {
            return object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = bindingAdapterPosition

                override fun getSelectionKey(): Long = binding.item?.id ?: -1L
            }
        }
    }
}
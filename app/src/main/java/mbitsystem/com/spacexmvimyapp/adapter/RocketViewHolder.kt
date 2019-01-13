package mbitsystem.com.spacexmvimyapp.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_rocket.view.*
import mbitsystem.com.spacexmvimyapp.data.model.Rocket

class RocketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(rocket: Rocket) {
        with(itemView) {
            rocketNameTextView.text = rocket.name
            Glide.with(this).load(rocket.photoUrl).into(rocketImageView)
        }
    }
}
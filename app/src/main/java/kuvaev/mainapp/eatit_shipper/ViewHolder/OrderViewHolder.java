package kuvaev.mainapp.eatit_shipper.ViewHolder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import kuvaev.mainapp.eatit_shipper.R;

public class OrderViewHolder extends RecyclerView.ViewHolder{
    public TextView txtOrderId, txtOrderStatus, txtOrderPhone, txtOrderAddress, txtOrderDate, txtOrderName, txtOrderPrice;
    public Button btnShipping;

    public OrderViewHolder(View itemView) {
        super(itemView);

        txtOrderId = itemView.findViewById(R.id.order_id);
        txtOrderStatus = itemView.findViewById(R.id.order_status);
        txtOrderPhone = itemView.findViewById(R.id.order_phone);
        txtOrderAddress = itemView.findViewById(R.id.order_address);
        txtOrderDate = itemView.findViewById(R.id.order_date);
        txtOrderName = itemView.findViewById(R.id.order_name);
        txtOrderPrice = itemView.findViewById(R.id.order_price);

        btnShipping = itemView.findViewById(R.id.btnShipping);
    }
}

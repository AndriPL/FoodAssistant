package com.slightlynonprofessionalprogrammer.foodassistant.ui.main

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import com.slightlynonprofessionalprogrammer.foodassistant.R
import com.slightlynonprofessionalprogrammer.foodassistant.ui.login.LoginActivity
import java.lang.Exception
import com.slightlynonprofessionalprogrammer.foodassistant.data.Product
import com.slightlynonprofessionalprogrammer.foodassistant.ui.addProduct.AddProductActivity
import java.io.IOException
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.slightlynonprofessionalprogrammer.foodassistant.data.ProductAdapter
import com.slightlynonprofessionalprogrammer.foodassistant.data.ProductComparator
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener  {

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private val ADD_PRODUCT = 24190
    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<Product>
    private lateinit var productAdapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.product_recyclerview_main_activity)
        productList = ArrayList()
        productAdapter = ProductAdapter(
            this,
            productList
        )

        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.setLayoutManager(mLayoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.setAdapter(productAdapter)

        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)

        auth = FirebaseAuth.getInstance()
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        user = auth.currentUser
        if (user == null) {
            changeUItoLogin()
        } else {
            auth.currentUser?.reload()
            try{
                fetchProducts()
            } catch (e: FirebaseNoSignedInUserException) {
                Log.e(TAG, "User is not signed in.", e)
                changeUItoLogin()
            }
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is ProductAdapter.MyViewHolder) {
            val user = auth.currentUser
            if (user == null) changeUItoLogin()
            if(productList.isNotEmpty()) {
                val position = viewHolder.getAdapterPosition()
                val product = productList.get(position)
                val productID = product.productID
                productAdapter.removeItem(position)
                FirebaseFirestore.getInstance().collection("products").document(productID).delete()
                    .addOnCompleteListener {
                        productAdapter.notifyDataSetChanged()
                        Log.d(TAG,"Product $productID deleted SUCCESSFULLY")
                    }
                    .addOnFailureListener {e ->
                        Log.e(TAG, "Product $productID deletion FAILED", e)
                        productList.add(product)
                        productAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext, "Error! Cannot delete product on server.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                productAdapter.notifyDataSetChanged()
                Log.d(TAG , "Product List empty? ProductList size = ${productList.size}. " +
                        "ProductList adapter item count = ${productAdapter.itemCount}, " +
                        "Products: ${productList.forEach { it.productName + " " } }")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.navigation_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_sign_out -> signOut()
            //TODO - add product
            R.id.menu_add_product -> {
                try {
                    if(auth.currentUser != null)
                    {
                        Log.d(TAG, "Changing activity to AddProduct")
                        val intent = Intent(this,  AddProductActivity::class.java)
                        intent.putExtra("userID", (auth.currentUser)!!.uid)
                        startActivityForResult(intent, ADD_PRODUCT)
                    } else {
                        Log.e(TAG, "In cannot add product to database, because user is not signed in.")
                    }
                } catch (e: FirebaseNoSignedInUserException) {
                    Log.e(TAG, "Adding new product failed. User is not signed in.", e)
                    changeUItoLogin()
                } catch (e: Exception) {
                    Log.e(TAG, "Adding new product failed. Unknown error ocurred", e)
                }


            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult called")
        if(requestCode == ADD_PRODUCT && resultCode == Activity.RESULT_OK) {
            try{
                Log.d(TAG, "Product received")
                val product = data!!.getSerializableExtra("product") as Product
                Log.d(TAG, "Product: ${product.productName}")
                addProductToDatabase(product)
            } catch (e: IOException){
                Log.e(TAG, "Creating picture FAILED in fun onActivityResult", e)
            }
        }
    }

    private fun addProductToDatabase(product: Product) {

            val database = FirebaseFirestore.getInstance()
            database.collection("products").document("${product.productID}")
                .set(product)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully saved product ${product.productName}  of user ${product.userID} to database")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failure when saving product to database", e)
                }
    }


    private fun fetchProducts() {
        //This function checks if there are any new products on the server and displays them in main activity
        val user = auth.currentUser
        if (user == null) {
            changeUItoLogin()
        }
        val collectionRef = FirebaseFirestore.getInstance().collection("products")
            .whereEqualTo("userID", user!!.uid) //TODO - products shared between users
        collectionRef.addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (querySnapshot != null) {
                productList.clear()
                querySnapshot.documents.forEach { documentSnapshot ->
                    if (documentSnapshot.exists() && documentSnapshot.data!!.isNotEmpty()) {
                        val source =
                            if (documentSnapshot.metadata.isFromCache) "Local"
                            else "Server"
                        val product: Product = documentSnapshot.toObject(Product::class.java)!!
                        productList.add(product)
                        Log.d(TAG,"Fetched new data. Source: $source. Data: ${documentSnapshot.data}"
                        )
                    }
                    sortListByExpiryDate(productList)
                    productAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d(TAG, "No data to fetch")
            }
        }
    }

    private fun sortListByExpiryDate(list: ArrayList<Product>) {
        Collections.sort(list, ProductComparator())
    };

    private fun signOut() {
        Log.d(TAG, "Starting to sign out")
        auth.signOut()
        changeUItoLogin()
        Log.d(TAG, "Successfully signed out")
    }

    private fun changeUItoLogin() {
        Log.d(TAG, "Changing activity to Login")
        val intent = Intent(this,  LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "Main Activity"
    }
}

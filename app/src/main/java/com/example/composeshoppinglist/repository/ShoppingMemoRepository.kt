package com.example.composeshoppinglist.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.composeshoppinglist.database.ShoppingMemo
import com.example.composeshoppinglist.database.ShoppingMemoDao
import com.example.composeshoppinglist.database.ShoppingMemoDatabase

class ShoppingMemoRepository(app: Application) {
    private var shoppingMemoDao: ShoppingMemoDao
    private var allShoppingMemos: LiveData<List<ShoppingMemo>>

    init {
        val db = ShoppingMemoDatabase.Factory.getInstance(app.applicationContext)
        shoppingMemoDao = db.shoppingMemoDao()
        allShoppingMemos = shoppingMemoDao.getAllShoppingMemos()
    }

    fun getAllShoppingMemos(): LiveData<List<ShoppingMemo>>{
        return allShoppingMemos
    }

    suspend fun insertOrUpdate(memo: ShoppingMemo){
        shoppingMemoDao.insertOrUpdate(memo)
    }

    suspend fun delete(memo: ShoppingMemo){
        shoppingMemoDao.delete(memo)
    }
}
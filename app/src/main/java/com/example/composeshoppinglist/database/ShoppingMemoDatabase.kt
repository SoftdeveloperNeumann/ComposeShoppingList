package com.example.composeshoppinglist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [ShoppingMemo::class], version = 1)
abstract class ShoppingMemoDatabase : RoomDatabase() {

    abstract fun shoppingMemoDao() : ShoppingMemoDao

    object Factory{
        private  var instance: ShoppingMemoDatabase? = null

        fun getInstance(context: Context): ShoppingMemoDatabase {
            if(instance == null){
                synchronized(ShoppingMemoDatabase::class){
                    if(instance == null){
                        instance = Room.databaseBuilder(
                            context.applicationContext,
                            ShoppingMemoDatabase::class.java,
                            "shopping_memos"
                        ).fallbackToDestructiveMigration().build()
                    }
                }
            }
            return instance!!
        }
    }
}
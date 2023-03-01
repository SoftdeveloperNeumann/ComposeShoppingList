package com.example.composeshoppinglist

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.composeshoppinglist.MutableValues.currentMemo
import com.example.composeshoppinglist.MutableValues.product
import com.example.composeshoppinglist.MutableValues.quantity
import com.example.composeshoppinglist.MutableValues.showDialog
import com.example.composeshoppinglist.MutableValues.showResult
import com.example.composeshoppinglist.database.ShoppingMemo
import com.example.composeshoppinglist.ui.theme.ComposeShoppingListTheme
import com.example.composeshoppinglist.viewmodel.ShoppingMemoViewModel

lateinit var shoppingMemoViewModel: ShoppingMemoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        shoppingMemoViewModel = ShoppingMemoViewModel.invoke(application)
        setContent {
            ComposeShoppingListTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainView()
                }
            }
        }
    }
}

object MutableValues : ViewModel() {
    var quantity by mutableStateOf("")
    var product by mutableStateOf("")
    var showDialog by mutableStateOf(false)
    var showResult by mutableStateOf(false)
    var currentMemo by mutableStateOf(ShoppingMemo(0,""))
}


@Composable
fun MainView() {
    Column(modifier = Modifier.padding(8.dp)) {
        InputArea()
        ListView()
    }
}

@Composable
fun InputArea() {
    Row() {
        TextField(value = quantity,
            onValueChange = { quantity = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .height(48.dp)
                .width(64.dp),
            placeholder = {
                Text(
                    text = "Anz.",
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        )
        TextField(value = product,
            onValueChange = { product = it },
            modifier = Modifier
                .height(48.dp)
                .weight(1f)
                .padding(start = 4.dp),
            placeholder = {
                Text(
                    text = "Artikel",
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        )
        Button(
            onClick = {
                shoppingMemoViewModel.insertOrUpdate(ShoppingMemo(quantity.toInt(), product))
                quantity = ""
                product = ""
            },
            modifier = Modifier
                .padding(start = 4.dp)
                .height(48.dp)
        ) {
            Text(
                text = "+",
                style = TextStyle(fontSize = 24.sp)

            )
        }
    }
}

@Composable
fun ListView() {
    val memoList by shoppingMemoViewModel.getAllShoppingMemos()!!.observeAsState(emptyList())

    LazyColumn(
        Modifier.padding(top = 4.dp)
    ) {
        items(memoList) {
            ListItem(memo = it)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ListItem(memo: ShoppingMemo) {
    val context = LocalContext.current
    var isChecked by remember {
        mutableStateOf(memo.isSelected)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            Modifier
                .weight(3f)
                .align(Alignment.Bottom)
        ) {
            ClickableText(text = buildAnnotatedString { append(memo.toString()) },
                style = TextStyle(
                    color = if(isChecked) Color.LightGray else Color.Black,
                    textDecoration = if(isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 24.sp,
                ),

                onClick = {
                    Toast.makeText(context, "$memo", Toast.LENGTH_SHORT).show()
                    showDialog = true
                   currentMemo = memo

                })
            if (showDialog){

                CreateDialog(memo = currentMemo)
            }
        }
        Column(
            Modifier
                .weight(1f)
                .align(Alignment.Bottom)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    memo.isSelected = it
                    shoppingMemoViewModel.insertOrUpdate(memo)
                },
                modifier = Modifier.align(Alignment.End)
            )
        }

    }

}

@Composable
fun CreateDialog(memo: ShoppingMemo){
    quantity = memo.quantity.toString()
    product = memo.product
    AlertDialog(onDismissRequest = { showDialog= false },
            title = { Text(text = "Artikel $memo ändern") },
        text =  {
            Column() {
                TextField(value = quantity, onValueChange = { quantity = it}, modifier =  Modifier.fillMaxWidth())
                TextField(value = product, onValueChange = { product = it}, modifier =  Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
                        Button(onClick = {
                            showResult = true
                            showDialog = false
                            memo.quantity = quantity.toInt()
                            memo.product = product
                            ShoppingMemoViewModel.insertOrUpdate(memo)
                            quantity = ""
                            product = ""
                        }) {
                            Text(text = "OK")
                        }

        },
        dismissButton = {
            Button(onClick = {
                showDialog = false
                showResult = false
                quantity = ""
                product = ""
            }) {
                Text(text = "Cancel")
            }
        }

        )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeShoppingListTheme {
        MainView()
    }
}
package com.example.composeshoppinglist


import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
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
    var currentMemo by mutableStateOf(ShoppingMemo(0, ""))
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
    Row {
        val focusRequester = remember { FocusRequester() }
        val focusRequester2 = remember { FocusRequester() }
        TextField(
            value = quantity,
            onValueChange = {
                quantity = it.take(3).trim()
                if (quantity.isBlank() || !quantity.isDigitsOnly()) {
                    quantity = ""
                    focusRequester.requestFocus()
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                .copy(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusRequester2.requestFocus() }),
            modifier = Modifier
                .height(56.dp)
                .width(64.dp)
                .focusRequester(focusRequester),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Anz.",
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        )

        TextField(
            value = product,
            onValueChange = {
                product = it
                if (product.isBlank()) {
                    focusRequester2.requestFocus()
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {

                if (quantity.isNotBlank() && product.isNotBlank()) {
                    shoppingMemoViewModel.insertOrUpdate(ShoppingMemo(quantity.toInt(), product))
                    quantity = ""
                    product = ""
                }
                if (quantity.isBlank()) {
                    focusRequester.requestFocus()
                    return@KeyboardActions
                }
                if (product.isBlank())
                    focusRequester2.requestFocus()

            }, onNext = { focusRequester2.requestFocus() }),
            modifier = Modifier
                .height(56.dp)
                .weight(1f)
                .padding(start = 4.dp)
                .focusRequester(focusRequester2),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Artikel",
                    style = TextStyle(fontSize = 10.sp, color = Color.Gray)
                )
            }
        )
        Button(
            onClick = {


                if (quantity.isNotBlank() && product.isNotBlank()) {
                    shoppingMemoViewModel.insertOrUpdate(ShoppingMemo(quantity.toInt(), product))
                    quantity = ""
                    product = ""
                }
                if (quantity.isBlank()) {
                    focusRequester.requestFocus()
                    return@Button
                }
                if (product.isBlank())
                    focusRequester2.requestFocus()
            },
            modifier = Modifier
                .padding(start = 4.dp)
                .height(56.dp)
        ) {
            Text(
                text = "+",
                style = TextStyle(fontSize = 24.sp)

            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListView() {
    val memoList by shoppingMemoViewModel.getAllShoppingMemos()!!.observeAsState(emptyList())
    LazyColumn(
        Modifier.padding(top = 4.dp)
    ) {
        items(memoList) { memo_ ->

            ListItem(memo = memo_)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun ListItem(memo: ShoppingMemo) {
    Log.d("TAG", "In ListItem: $memo")
    val context = LocalContext.current
    var isChecked by remember {
        mutableStateOf(memo.isSelected)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {

        Column(
            Modifier
                .weight(1f)
            //.align(Alignment.Bottom)
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    memo.isSelected = it
                    shoppingMemoViewModel.insertOrUpdate(memo)
                },
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Column(
            Modifier
                .weight(6f)
            //.align(Alignment.Top)
        ) {
            ClickableText(text = buildAnnotatedString { append(memo.toString()) },
                style = TextStyle(
                    color = if (isChecked) Color.LightGray else Color.Black,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    fontSize = 24.sp,
                ),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    Toast.makeText(context, "$memo", Toast.LENGTH_SHORT).show()
                    showDialog = true
                    currentMemo = memo

                })

            if (showDialog) {

                CreateDialog(memo = currentMemo)
            }
        }

        Column() {
            IconButton(
                onClick = {
                          shoppingMemoViewModel.delete(memo)
                          },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Löschen"
                )
            }
        }
    }

}

//region für SwipeTo Dismiss
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun ListView() {
//    val memoList by shoppingMemoViewModel.getAllShoppingMemos()!!.observeAsState(emptyList())
//    LazyColumn(
//        Modifier.padding(top = 4.dp)
//    ) {
//        items(memoList) { memo_ ->
//
//            var unread by remember {
//                mutableStateOf(false)
//            }
//            val dismissState = rememberDismissState(
//                confirmStateChange = {
//                    if (it == DismissValue.DismissedToStart) {
//                        unread = !unread
//
//
//                        Log.d("TAG", "ListView: ${memoList.contains(memo_)}")
//                    }
//                    it != DismissValue.DismissedToEnd
//                }
//            )
//
//
//            SwipeToDismiss(
//                state = dismissState,
//                directions = setOf(DismissDirection.EndToStart),
//                background = {
//                    val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
//                    if(direction == DismissDirection.EndToStart && !unread){
//                        Box(
//                            contentAlignment = Alignment.CenterEnd,
//                            modifier =
//                            Modifier
//                                .fillMaxSize()
//                                .background(Color.LightGray)
//                                .padding(horizontal = 20.dp),
//
//                            ){
//                            Icon( Icons.Default.Delete,
//                                contentDescription ="",
//                            modifier =  Modifier.scale(if (dismissState.targetValue == DismissValue.Default)0.75f else 1f))
//                        }
//                    }
//                },
//                dismissContent = {
//                    Log.d("TAG", "Call ListItem: $memo_")
//                    if(unread && dismissState.isDismissed(DismissDirection.EndToStart)){
//                        shoppingMemoViewModel.delete(memo_)
////                        unread = !unread
//                    }else{
//                        ListItem(memo = memo_)
//                        Spacer(modifier = Modifier.height(4.dp))
//                    }
//
//                },
//            )
//
//
//        }
//    }
//}

//@Composable
//fun ListItem(memo: ShoppingMemo) {
//    Log.d("TAG", "In ListItem: $memo")
//    val context = LocalContext.current
//    var isChecked by remember {
//        mutableStateOf(memo.isSelected)
//    }
//
//    Row(verticalAlignment = Alignment.CenterVertically) {
//        Column(
//            Modifier
//                .weight(3f)
//                .align(Alignment.Bottom)
//        ) {
//            ClickableText(text = buildAnnotatedString { append(memo.toString()) },
//                style = TextStyle(
//                    color = if (isChecked) Color.LightGray else Color.Black,
//                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
//                    fontSize = 24.sp,
//                ),
//                modifier = Modifier.fillMaxWidth(),
//                onClick = {
//                    Toast.makeText(context, "$memo", Toast.LENGTH_SHORT).show()
//                    showDialog = true
//                    currentMemo = memo
//
//                })
//            if (showDialog) {
//
//                CreateDialog(memo = currentMemo)
//            }
//        }
//        Column(
//            Modifier
//                .weight(1f)
//                .align(Alignment.Bottom)
//        ) {
//            Checkbox(
//                checked = isChecked,
//                onCheckedChange = {
//                    isChecked = it
//                    memo.isSelected = it
//                    shoppingMemoViewModel.insertOrUpdate(memo)
//                },
//                modifier = Modifier.align(Alignment.End)
//            )
//        }
//
//    }
//
//}
//endregion

@Composable
fun CreateDialog(memo: ShoppingMemo) {
    quantity = memo.quantity.toString()
    product = memo.product
    AlertDialog(onDismissRequest = { showDialog = false },
        title = { Text(text = "Artikel $memo ändern") },
        text = {
            Column {
                TextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = product,
                    onValueChange = { product = it },
                    modifier = Modifier.fillMaxWidth()
                )
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
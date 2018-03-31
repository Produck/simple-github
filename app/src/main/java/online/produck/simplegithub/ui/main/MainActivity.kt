package online.produck.simplegithub.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View

import online.produck.simplegithub.R
import online.produck.simplegithub.ui.search.SearchActivity

class MainActivity : Activity() {

    internal lateinit var btnSearch: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSearch = findViewById(R.id.btnActivityMainSearch)
        btnSearch.setOnClickListener { startActivity(Intent(this@MainActivity, SearchActivity::class.java)) }
    }
}

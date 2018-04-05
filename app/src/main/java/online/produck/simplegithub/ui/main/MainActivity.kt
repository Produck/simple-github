package online.produck.simplegithub.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*

import online.produck.simplegithub.R
import online.produck.simplegithub.ui.search.SearchActivity
import org.jetbrains.anko.startActivity

class MainActivity : Activity() {

    internal lateinit var btnSearch: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnActivityMainSearch.setOnClickListener {
            startActivity<SearchActivity>()
        }

        btnSearch = findViewById(R.id.btnActivityMainSearch)
    }
}

package studio.codable.unpause.screens.activity.register

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_register.*
import studio.codable.unpause.R
import studio.codable.unpause.base.activity.BaseActivity
import studio.codable.unpause.screens.activity.home.HomeActivity
import javax.inject.Inject

class RegisterActivity : BaseActivity() {

    @Inject
    lateinit var vmFactory: ViewModelProvider.Factory
    private val registerVm: RegisterViewModel by viewModels { vmFactory }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, RegisterActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initUI()
        initObservers()
    }

    private fun initObservers() {
        registerVm.errors.observe(this, Observer {
            showError(it.getContentIfNotHandled())
        })

        registerVm.userId.observe(this, Observer {
            startActivity(HomeActivity.getIntent(this, it))
        })
    }

    private fun initUI() {

        btn_register.setOnClickListener {
            if (checkFields()) {
                registerVm.register(
                    text_email.text.toString(),
                    text_password.text.toString(),
                    text_first_name.text.toString(),
                    text_last_name.text.toString()
                )
            } else {
                showMessage("Invalid input")
            }
        }
    }

    private fun checkFields(): Boolean {
        return text_email.text.isNotBlank() && text_password.text.isNotBlank()
    }
}
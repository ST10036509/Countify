package st10036509.countify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import st10036509.countify.service.NavigationService
import st10036509.countify.user_interface.account.LoginFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initialise singleton navigation service upon application start and provide the Fragment Manager
        NavigationService.initialise(supportFragmentManager)

        // sdd the LoginFragment when the activity is first created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }
}
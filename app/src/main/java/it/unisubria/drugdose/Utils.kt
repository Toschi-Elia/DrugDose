package it.unisubria.drugdose

import android.util.Patterns


 class Utils {


}
fun String.isValidEmail(): Boolean
{
    return this.trim().isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this.trim()).matches()
}
fun String.isStrongPassword():Boolean{
    val passwordRegex= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\\-])(?=\\S+$).{8,}$"
    return this.matches(passwordRegex.toRegex())
}
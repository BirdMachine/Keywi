package com.dessalines.thumbkey.inputcontext

import android.content.Context

data class ContextEngineSettings(val adaptToField:Boolean=true,val smartEnter:Boolean=true,val suppressSensitiveSuggestions:Boolean=true,val preserveEmailAndUrlTokens:Boolean=true)
object ContextEnginePreferences {
 private const val P="context_engine_preferences"; private const val A="adapt_to_field"; private const val S="smart_enter"; private const val X="suppress_sensitive_suggestions"; private const val T="preserve_email_and_url_tokens"
 fun load(c:Context):ContextEngineSettings { val p=c.getSharedPreferences(P,Context.MODE_PRIVATE); return ContextEngineSettings(p.getBoolean(A,true),p.getBoolean(S,true),p.getBoolean(X,true),p.getBoolean(T,true)) }
 fun setAdaptToField(c:Context,v:Boolean){c.getSharedPreferences(P,0).edit().putBoolean(A,v).apply()}
 fun setSmartEnter(c:Context,v:Boolean){c.getSharedPreferences(P,0).edit().putBoolean(S,v).apply()}
 fun setSuppressSensitiveSuggestions(c:Context,v:Boolean){c.getSharedPreferences(P,0).edit().putBoolean(X,v).apply()}
 fun setPreserveEmailAndUrlTokens(c:Context,v:Boolean){c.getSharedPreferences(P,0).edit().putBoolean(T,v).apply()}
}

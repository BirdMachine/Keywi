package com.dessalines.thumbkey.inputcontext

import android.view.inputmethod.EditorInfo

enum class SmartEnterBehavior { NEWLINE, IME_ACTION, LEGACY }
data class InputCapabilities(val isSensitive:Boolean,val canOfferSuggestions:Boolean,val canOfferInputPalettes:Boolean,val canTransformSelection:Boolean,val canReplaceSelection:Boolean,val supportsNewline:Boolean,val prefersImeAction:Boolean,val smartEnterBehavior:SmartEnterBehavior,val shouldPreserveStructuredToken:Boolean)
object ContextEnginePolicy {
 fun evaluate(context:InputContext,settings:ContextEngineSettings=ContextEngineSettings()):InputCapabilities {
  val adaptive=settings.adaptToField; val sensitive=context.isPassword; val editable=!context.isReadOnly; val textLike=context.fieldKind==FieldKind.TEXT
  val action=context.imeOptions and EditorInfo.IME_MASK_ACTION; val noAction=context.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0
  val explicit=!noAction && action!=EditorInfo.IME_ACTION_NONE && action!=EditorInfo.IME_ACTION_UNSPECIFIED
  val newline=editable&&textLike&&(!adaptive||context.isMultiLine); val prefer=adaptive&&explicit&&!context.isMultiLine
  val enter=when { !settings.smartEnter||!adaptive->SmartEnterBehavior.LEGACY; context.isMultiLine&&newline->SmartEnterBehavior.NEWLINE; prefer->SmartEnterBehavior.IME_ACTION; else->SmartEnterBehavior.LEGACY }
  return InputCapabilities(sensitive,editable&&textLike&&!(sensitive&&settings.suppressSensitiveSuggestions),editable&&textLike&&!sensitive,editable&&textLike&&context.hasSelection&&!sensitive,editable&&context.hasSelection,newline,prefer,enter,adaptive&&settings.preserveEmailAndUrlTokens&&textLike)
 }
}

package com.github.windsekirun.playwithksp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.windsekirun.playwithksp.core.UpdateMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateMemberUseCase: UpdateMemberUseCase
) : ViewModel() {

    private val _errorData = MutableLiveData<Throwable>()
    val errorData: LiveData<Throwable> get() = _errorData

    fun init() {
        val ceh = CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable)
            _errorData.value = throwable
        }
        viewModelScope.launch(ceh) {
            val request = UpdateMemberUseCase.Request(
                "11",
                "",
                "",
                ""
            )

            updateMemberUseCase(request)
        }
    }
}
package com.github.windsekirun.playwithksp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.windsekirun.playwithksp.core.usecase.UpdateMemberUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val updateMemberUseCase: UpdateMemberUseCase
) : ViewModel() {
    val id = MutableLiveData<String>()
    val pw = MutableLiveData<String>()
    val age = MutableLiveData<String>()

    private val _errorData = MutableLiveData<Throwable>()
    val errorData: LiveData<Throwable> get() = _errorData

    private val _savedData = MutableLiveData<String>()
    val savedData: LiveData<String> get() = _savedData

    fun sendRequest() {
        val ceh = CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable)
            _errorData.value = throwable
        }

        viewModelScope.launch(ceh) {
            val request = UpdateMemberUseCase.Request(
                id.value.orEmpty(),
                pw.value.orEmpty(),
                age.value.orEmpty(),
            )

            val result = updateMemberUseCase(request)
            _savedData.value = result
        }
    }
}
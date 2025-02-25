package ru.otus.basicarchitecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.otus.basicarchitecture.databinding.FragmentNameBinding
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class NameFragment : Fragment() {
    val viewModel: NameViewModel by viewModels()
    private var _binding: FragmentNameBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNameBinding.inflate(
            inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Восстанавливаем сохраненные данные
        binding.nameInput.setText(viewModel.name.value)
        binding.surnameInput.setText(viewModel.surname.value)
        binding.birthDateInput.setText(viewModel.birthDate.value)

        binding.birthDateInput.addTextChangedListener(
            MaskWatcher("##.##.####", binding.birthDateInput))

        binding.nextButton.setOnClickListener {
            val birthDate = binding.birthDateInput.text.toString()
            if (!validateBirthDate(birthDate)) {
                Toast.makeText(requireContext(),
                    getString(R.string.you_must_be_18), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.name.value = binding.nameInput.text.toString()
            viewModel.surname.value = binding.surnameInput.text.toString()
            viewModel.birthDate.value = birthDate
            findNavController().navigate(R.id.action_nameFragment_to_addressFragment)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun validateBirthDate(date: String): Boolean {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        sdf.isLenient = false
        return try {
            val birthDate = sdf.parse(date) ?: return false
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -18)
            birthDate.before(calendar.time)
        } catch (e: ParseException) {
            false
        }
    }
}
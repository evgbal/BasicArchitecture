package ru.otus.basicarchitecture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.otus.basicarchitecture.databinding.FragmentSummaryBinding

@AndroidEntryPoint
class SummaryFragment : Fragment() {
    private val viewModel: SummaryViewModel by viewModels()
    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подписка на данные из ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.name.collectLatest { binding.tvName.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.surname.collectLatest { binding.tvSurname.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.birthDate.collectLatest { binding.tvDob.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.address.collectLatest { binding.tvAddress.text = it }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.interests.collectLatest { updateInterests(it) }
        }
    }

    private fun updateInterests(interests: List<String>) {
        binding.flInterests.removeAllViews()
        for (interest in interests) {
            val chip = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_chip, binding.flInterests, false) as TextView
            chip.text = interest
            binding.flInterests.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

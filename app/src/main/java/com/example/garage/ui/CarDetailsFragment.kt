package com.example.garage.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.garage.BaseApplication
import com.example.garage.R
import com.example.garage.databinding.FragmentCarDetailsBinding
import com.example.garage.viewmodel.CarViewModel
import com.example.garage.viewmodel.CarViewModelFactory

class CarDetailsFragment : Fragment() {

    private val viewModel: CarViewModel by activityViewModels {
        CarViewModelFactory(requireActivity().application,
            (requireActivity().application as BaseApplication).database.carDao())
    }

    private var _binding: FragmentCarDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCarDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedLogo.observe(viewLifecycleOwner) {
            binding.apply {
                icBrand.load(viewModel.selectedLogo.value) {
                    placeholder(R.drawable.loading_animation)
                    error(R.drawable.ic_connection_error)
                    crossfade(true)
                }
                textViewModel.text = viewModel.car.value?.model
                textViewBrand.text = viewModel.car.value?.brand
                textViewDisplacement.text = viewModel.car.value?.displacement
                textViewPowerSupply.text = viewModel.car.value?.powerSupply
                textViewKm.text = viewModel.car.value?.km.toString()
                textViewYear.text = viewModel.car.value?.year.toString()

                editCar.setOnClickListener {
                    val action = CarListFragmentDirections.actionCarListFragmentToAddEditCarFragment(viewModel.car.value!!.id)
                    findNavController().navigate(action)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

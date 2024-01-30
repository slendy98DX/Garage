package com.example.garage.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.NumberPicker
import android.widget.Spinner
import androidx.core.view.isNotEmpty
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.garage.BaseApplication
import com.example.garage.R
import com.example.garage.database.models.Car
import com.example.garage.databinding.FragmentAddEditCarBinding
import com.example.garage.enums.CarDisplacementEnum
import com.example.garage.enums.CarPowerSupplyEnum
import com.example.garage.network.CarModelApi
import com.example.garage.viewmodel.CarViewModel
import com.example.garage.viewmodel.CarViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddEditCarFragment : Fragment() {

    private val viewModel: CarViewModel by activityViewModels {
        CarViewModelFactory(
            requireActivity().application,
            (requireActivity().application as BaseApplication).database.carDao()
        )
    }

    private val navigationArgs: AddEditCarFragmentArgs by navArgs()

    private var carModels: List<String> = emptyList()

    private var _binding: FragmentAddEditCarBinding? = null

    private var modelSpinner: Spinner? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCarBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.sharedViewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState != null) {
            savedInstanceState?.getInt(KEY_SELECTED_MODEL_POSITION)?.let { position ->
                modelSpinner?.setSelection(position)
            }
        }

        viewModel.selectedBrand.observe(viewLifecycleOwner) { selectedBrand ->
            if (!selectedBrand.isNullOrBlank()) {
                runBlocking {
                    fetchAndFilterCarModels(selectedBrand)
                }
            }
        }

        val displacementSpinner: Spinner = binding.displacementSpinner

        val displacementOptions = CarDisplacementEnum.values().map { it.formattedValue }

        val displacementAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            displacementOptions
        )

        displacementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
        displacementSpinner.adapter = displacementAdapter

        displacementSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedDisplacement = parent?.getItemAtPosition(position).toString()
                    viewModel.setSelectedDisplacement(selectedDisplacement)
                    setupSaveButtonState()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        val yearPicker: NumberPicker = binding.yearPicker

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 50
        yearPicker.maxValue = currentYear

        val displayValues = Array(51) { (currentYear - 50 + it).toString() }
        yearPicker.displayedValues = displayValues

        yearPicker.value = currentYear

        viewModel.year.observe(viewLifecycleOwner) { year ->
            yearPicker.value = year
        }

        yearPicker.setOnValueChangedListener { _, _, year ->
            viewModel.setYear(year)
        }


        val powerSupplySpinner: Spinner = binding.powerSupplySpinner

        val powerSupplyOptions = CarPowerSupplyEnum.values().map { it.text }
        val powerSupplyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            powerSupplyOptions
        )
        powerSupplyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        powerSupplySpinner.adapter = powerSupplyAdapter

        powerSupplySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedPowerSupply = parent.getItemAtPosition(position).toString()
                    viewModel.setSelectedPowerSupply(selectedPowerSupply)
                    setupSaveButtonState()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }

        modelSpinner = binding.modelInput

        modelSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedModel = parent.getItemAtPosition(position).toString()
                viewModel.setSelectedCarModel(selectedModel)
                setupSaveButtonState()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val kmInput: TextInputEditText = binding.kmInput

        kmInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setupSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        if (navigationArgs.id < 1) {
            binding.deleteBtn.visibility = View.GONE
            binding.saveBtn.setOnClickListener {
                saveCar()
            }
        } else {
            viewModel.setSelectedBrand(viewModel.car.value!!.brand)
            updateModelSpinner(carModels) {
                setupSaveButtonState()
            }
            displacementSpinner.setSelection(displacementOptions.indexOf(viewModel.car.value!!.displacement))
            kmInput.setText(viewModel.car.value!!.km.toString())
            powerSupplySpinner.setSelection(powerSupplyOptions.indexOf(viewModel.car.value!!.powerSupply))
            yearPicker.value = viewModel.car.value!!.year
            binding.apply {
                saveBtn.setOnClickListener {
                    if (binding.kmInput.text.toString().toDouble().minus(viewModel.car.value!!.km) > 15000) {
                        viewModel.scheduleReminder(viewModel.car.value!!.model)
                    }
                    viewModel.setCar(
                        Car(
                            id = viewModel.car.value!!.id,
                            model = binding.modelInput.selectedItem.toString(),
                            brand = binding.brand.text.toString(),
                            displacement = binding.displacementSpinner.selectedItem.toString(),
                            km = binding.kmInput.text.toString().toDouble(),
                            powerSupply = binding.powerSupplySpinner.selectedItem.toString(),
                            year = binding.yearPicker.value
                        )
                    )
                    viewModel.updateCar(viewModel.car.value!!)
                    findNavController().navigate(R.id.action_addEditCarFragment_to_carListFragment)
                }
                deleteBtn.visibility = View.VISIBLE
                deleteBtn.setOnClickListener {
                    showDeleteConfirmationDialog()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to delete this car?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCar(viewModel.car.value!!)
                findNavController().navigate(R.id.action_addEditCarFragment_to_carListFragment)
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
        builder.create().show()
    }

    private fun fetchAndFilterCarModels(selectedBrand: String) {
        lifecycleScope.launch {
            try {
                val fetchedCarModels = CarModelApi.retrofitService.getCarModelList()

                val filteredCarModels = fetchedCarModels.filter { it.brand == selectedBrand }[0].models

                carModels = filteredCarModels

                updateModelSpinner(carModels) {
                    setupSaveButtonState()
                }
            } catch (e: Exception) {
                Log.e("AddEditCarFragment", "Error fetching car models: ${e.message}")
            }
        }
    }

    private fun saveCar() {
        val car = Car(
            model = viewModel.selectedCarModel.value!!,
            displacement = viewModel.selectedDisplacement.value!!,
            brand = viewModel.selectedBrand.value?.takeIf { it.isNotBlank() } ?: "Default Brand",
            km = binding.kmInput.text.toString().toDouble(),
            year = binding.yearPicker.value,
            powerSupply = viewModel.selectedPowerSupply.value ?: "Default Power Supply"
        )
        viewModel.addCar(car)
        findNavController().navigate(R.id.action_addEditCarFragment_to_carListFragment)
    }

    private fun setupSaveButtonState(){
        val isModelFilled = binding.modelInput.selectedItemPosition > 0
        val isDisplacementFilled = binding.displacementSpinner.selectedItemPosition > 0
        val isPowerSupplyFilled = binding.powerSupplySpinner.selectedItemPosition > 0
        val isBrandFilled = viewModel.selectedBrand.value?.isNotBlank() == true
        val isKmFilled = binding.kmInput.text?.isNotBlank() == true
        val isYearFilled = binding.yearPicker.isNotEmpty()

        val isAllFilled = isModelFilled && isDisplacementFilled &&
                isPowerSupplyFilled && isBrandFilled &&
                isKmFilled && isYearFilled

        binding.saveBtn.isEnabled = isAllFilled
    }

    @SuppressLint("SuspiciousIndentation")
    private fun updateModelSpinner(carModels: List<String>, callback: () -> Unit) {
        val modelTitles = mutableListOf("Select a Model")

        carModels.forEach { model ->
            modelTitles.add(model)
        }

        val modelAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            modelTitles
        )
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner?.adapter = modelAdapter

        viewModel.car.value?.let { car ->
            val index = carModels.indexOf(car.model)
            if (index != -1) {
                modelSpinner?.setSelection(index)
            }
        }

        modelSpinner?.post {
            callback()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the selected model position
        outState.putInt(KEY_SELECTED_MODEL_POSITION, modelSpinner!!.selectedItemPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_SELECTED_MODEL_POSITION = "selected_model_position"
    }

}

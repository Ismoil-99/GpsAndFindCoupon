package com.app.mapsexample.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.mapsexample.DESCCOUPON
import com.app.mapsexample.DISTANCE
import com.app.mapsexample.databinding.LayoutShowCouponBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetShowCoupon : BottomSheetDialogFragment() {
    private lateinit var binding: LayoutShowCouponBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutShowCouponBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 600
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val getDistance = requireArguments().getDouble(DISTANCE)
        val getTitle = requireArguments().getString(DESCCOUPON)
        binding.nameCoupon.text = getTitle
        binding.nameDistance.text = "${getDistance.toInt()} км"
    }
}
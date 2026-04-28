package com.answufeng.ui.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment

class TabFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val color = requireArguments().getInt(ARG_COLOR)

        view.findViewById<View>(R.id.root_layout).setBackgroundColor(color)
        view.findViewById<TextView>(R.id.tv_title).text = title
        view.findViewById<TextView>(R.id.tv_subtitle).apply {
            text = "滑动验证指示器、点击验证选中回调、重进页面验证状态恢复。"
            setTextColor(ColorUtils.setAlphaComponent(0xFF1F2430.toInt(), 180))
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_COLOR = "color"

        fun newInstance(title: String, color: Int): TabFragment {
            return TabFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putInt(ARG_COLOR, color)
                }
            }
        }
    }
}

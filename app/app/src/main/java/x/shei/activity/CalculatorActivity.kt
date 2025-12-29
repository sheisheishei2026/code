package x.shei.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import x.shei.R
import x.shei.util.ImmersedUtil
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorActivity : AppCompatActivity() {
    
    private lateinit var tvFormula: TextView
    private lateinit var tvResult: TextView
    
    private var currentInput = StringBuilder()
    private var formula = StringBuilder()
    private var lastOperator: String? = null
    private var lastNumber: String = "0"
    private var shouldResetInput = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)
        ImmersedUtil.setImmersedMode(this, false)
        
        initViews()
        setupButtons()
    }
    
    private fun initViews() {
        tvFormula = findViewById(R.id.tvFormula)
        tvResult = findViewById(R.id.tvResult)
        updateDisplay()
    }
    
    private fun setupButtons() {
        // 数字按钮
        findViewById<Button>(R.id.btn0).setOnClickListener { onNumberClick("0") }
        findViewById<Button>(R.id.btn1).setOnClickListener { onNumberClick("1") }
        findViewById<Button>(R.id.btn2).setOnClickListener { onNumberClick("2") }
        findViewById<Button>(R.id.btn3).setOnClickListener { onNumberClick("3") }
        findViewById<Button>(R.id.btn4).setOnClickListener { onNumberClick("4") }
        findViewById<Button>(R.id.btn5).setOnClickListener { onNumberClick("5") }
        findViewById<Button>(R.id.btn6).setOnClickListener { onNumberClick("6") }
        findViewById<Button>(R.id.btn7).setOnClickListener { onNumberClick("7") }
        findViewById<Button>(R.id.btn8).setOnClickListener { onNumberClick("8") }
        findViewById<Button>(R.id.btn9).setOnClickListener { onNumberClick("9") }
        
        // 运算符按钮
        findViewById<Button>(R.id.btnAdd).setOnClickListener { onOperatorClick("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { onOperatorClick("−") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { onOperatorClick("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { onOperatorClick("÷") }
        
        // 功能按钮
        findViewById<Button>(R.id.btnEquals).setOnClickListener { onEqualsClick() }
        findViewById<Button>(R.id.btnClear).setOnClickListener { onClearClick() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { onDeleteClick() }
        findViewById<Button>(R.id.btnDecimal).setOnClickListener { onDecimalClick() }
    }
    
    private fun onNumberClick(number: String) {
        if (shouldResetInput) {
            currentInput.clear()
            shouldResetInput = false
        }
        
        // 防止多个前导零
        if (currentInput.toString() == "0") {
            currentInput.clear()
        }
        
        currentInput.append(number)
        updateDisplay()
    }
    
    private fun onOperatorClick(operator: String) {
        if (currentInput.isNotEmpty()) {
            // 如果有当前输入，先计算结果
            if (lastOperator != null && lastNumber.isNotEmpty()) {
                val result = calculate(lastNumber, currentInput.toString(), lastOperator!!)
                lastNumber = result
                currentInput.clear()
                currentInput.append(result)
            } else {
                lastNumber = currentInput.toString()
            }
        }
        
        lastOperator = operator
        formula.clear()
        formula.append(lastNumber).append(" ").append(operator)
        shouldResetInput = true
        updateDisplay()
    }
    
    private fun onEqualsClick() {
        if (lastOperator != null && currentInput.isNotEmpty() && lastNumber.isNotEmpty()) {
            val result = calculate(lastNumber, currentInput.toString(), lastOperator!!)
            
            // 显示完整公式
            formula.clear()
            formula.append(lastNumber)
                .append(" ")
                .append(lastOperator)
                .append(" ")
                .append(currentInput.toString())
                .append(" = ")
                .append(result)
            
            // 更新结果
            currentInput.clear()
            currentInput.append(result)
            lastNumber = result
            lastOperator = null
            shouldResetInput = true
            
            updateDisplay()
        }
    }
    
    private fun onClearClick() {
        currentInput.clear()
        formula.clear()
        lastOperator = null
        lastNumber = "0"
        shouldResetInput = false
        updateDisplay()
    }
    
    private fun onDeleteClick() {
        if (currentInput.isNotEmpty()) {
            currentInput.deleteCharAt(currentInput.length - 1)
            updateDisplay()
        }
    }
    
    private fun onDecimalClick() {
        if (shouldResetInput) {
            currentInput.clear()
            shouldResetInput = false
        }
        
        if (currentInput.isEmpty()) {
            currentInput.append("0")
        }
        
        if (!currentInput.toString().contains(".")) {
            currentInput.append(".")
            updateDisplay()
        }
    }
    
    private fun calculate(num1: String, num2: String, operator: String): String {
        return try {
            val bd1 = BigDecimal(num1)
            val bd2 = BigDecimal(num2)
            val result = when (operator) {
                "+" -> bd1.add(bd2)
                "−" -> bd1.subtract(bd2)
                "×" -> bd1.multiply(bd2)
                "÷" -> {
                    if (bd2.compareTo(BigDecimal.ZERO) == 0) {
                        return "错误"
                    }
                    bd1.divide(bd2, 10, RoundingMode.HALF_UP)
                }
                else -> BigDecimal.ZERO
            }
            
            // 格式化结果，移除末尾的零
            val resultStr = result.stripTrailingZeros().toPlainString()
            // 如果结果太长，限制显示长度
            if (resultStr.length > 15) {
                result.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            } else {
                resultStr
            }
        } catch (e: Exception) {
            "错误"
        }
    }
    
    private fun updateDisplay() {
        val displayText = if (currentInput.isEmpty()) "0" else currentInput.toString()
        tvResult.text = displayText
        
        // 显示公式，如果有的话
        if (formula.isNotEmpty()) {
            tvFormula.text = formula.toString()
            tvFormula.visibility = View.VISIBLE
        } else {
            tvFormula.visibility = View.GONE
        }
    }
}


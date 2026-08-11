// src/pages/OTPVerification.tsx
import React, { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useTheme } from '../context/ThemeContext'
import Lottie from 'react-lottie-player'
import otpAnimation from '../assets/lottie/OTP-Verification.json'
import { 
  ArrowRightIcon,
  SunIcon,
  MoonIcon,
  CheckBadgeIcon,
  ExclamationCircleIcon
} from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'

const OTPVerification = () => {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [otp, setOtp] = useState(['', '', '', '', '', ''])
  const [isLoading, setIsLoading] = useState(false)
  const [timeLeft, setTimeLeft] = useState(60)
  const [canResend, setCanResend] = useState(false)
  const [error, setError] = useState('')
  const inputRefs = useRef<(HTMLInputElement | null)[]>([])

  useEffect(() => {
    if (timeLeft > 0 && !canResend) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000)
      return () => clearTimeout(timer)
    } else if (timeLeft === 0) {
      setCanResend(true)
    }
  }, [timeLeft, canResend])

  const handleChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return
    const newOtp = [...otp]
    newOtp[index] = value.slice(0, 1)
    setOtp(newOtp)
    setError('')

    if (value && index < 5) {
      inputRefs.current[index + 1]?.focus()
    }
  }

  const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus()
    }
    if (e.key === 'Enter') {
      handleSubmit(e)
    }
  }

  const handlePaste = (e: React.ClipboardEvent) => {
    e.preventDefault()
    const pastedData = e.clipboardData.getData('text').slice(0, 6)
    if (!/^\d*$/.test(pastedData)) return
    
    const newOtp = [...otp]
    for (let i = 0; i < pastedData.length && i < 6; i++) {
      newOtp[i] = pastedData[i]
    }
    setOtp(newOtp)
    
    const nextEmpty = newOtp.findIndex(val => val === '')
    if (nextEmpty !== -1) {
      inputRefs.current[nextEmpty]?.focus()
    } else {
      inputRefs.current[5]?.focus()
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const otpString = otp.join('')
    if (otpString.length !== 6) {
      setError('Please enter all 6 digits')
      return
    }

    setIsLoading(true)
    try {
      await new Promise(resolve => setTimeout(resolve, 1500))
      toast.success('OTP verified successfully!')
      navigate('/setup-profile')
    } catch (error) {
      setError('Invalid OTP. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleResend = () => {
    if (!canResend) return
    setTimeLeft(60)
    setCanResend(false)
    toast.success('OTP resent to your email')
  }

  const setInputRef = (el: HTMLInputElement | null, index: number) => {
    inputRefs.current[index] = el
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50/50 to-white dark:from-[#18191A] dark:to-[#1F2022] flex flex-col justify-center py-8 sm:py-12 px-4 sm:px-6 lg:px-8">
      {/* Theme Toggle */}
      <div className="absolute top-4 right-4 sm:top-6 sm:right-6">
        <button
          onClick={toggleTheme}
          className="p-2 rounded-full hover:bg-gray-200 dark:hover:bg-[#3A3B3C] transition-colors"
          aria-label="Toggle theme"
        >
          {theme === 'dark' ? (
            <SunIcon className="w-5 h-5 text-yellow-400" />
          ) : (
            <MoonIcon className="w-5 h-5 text-gray-700" />
          )}
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 max-w-5xl mx-auto w-full">
        {/* Left Column - Animation */}
        <div className="hidden lg:flex items-center justify-center">
          <div className="w-full max-w-md">
            <Lottie
              loop
              play
              animationData={otpAnimation}
              style={{ 
                width: '100%', 
                height: 'auto',
                display: 'block'
              }}
            />
          </div>
        </div>

        {/* Right Column - Form */}
        <div className="sm:mx-auto sm:w-full sm:max-w-md">
          <div className="flex justify-center lg:hidden">
            <div className="w-14 h-14 sm:w-16 sm:h-16 bg-gradient-to-r from-blue-500 to-blue-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <CheckBadgeIcon className="w-8 h-8 sm:w-10 sm:h-10 text-white" />
            </div>
          </div>
          <h2 className="mt-4 sm:mt-6 text-center text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white">
            Verify Your Email
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
            We've sent a 6-digit verification code to your email
          </p>
          <p className="text-center text-xs text-gray-500 dark:text-[#B0B3B8] mt-1">
            Check your spam folder if you don't see it
          </p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mt-6 sm:mt-8 bg-white dark:bg-[#242526] py-8 px-6 sm:px-10 shadow-xl rounded-2xl border border-gray-100 dark:border-[#3E4042]"
          >
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* OTP Input Fields */}
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB] text-center mb-4">
                  Enter Verification Code
                </label>
                <div className="flex justify-center gap-2 sm:gap-3">
                  {otp.map((digit, index) => (
                    <input
                      key={index}
                      ref={(el) => setInputRef(el, index)}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      value={digit}
                      onChange={(e) => handleChange(index, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(index, e)}
                      onPaste={index === 0 ? handlePaste : undefined}
                      className={`w-10 h-12 sm:w-12 sm:h-14 text-center text-lg sm:text-xl font-semibold rounded-xl border ${
                        error ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 dark:border-[#3E4042] focus:ring-blue-500'
                      } bg-white dark:bg-[#3A3B3C] text-gray-900 dark:text-white focus:outline-none focus:ring-2 transition-all`}
                      autoFocus={index === 0}
                    />
                  ))}
                </div>
                {error && (
                  <p className="mt-3 text-sm text-red-500 text-center flex items-center justify-center">
                    <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                    {error}
                  </p>
                )}
              </div>

              {/* Timer & Resend */}
              <div className="text-center">
                {!canResend ? (
                  <p className="text-sm text-gray-600 dark:text-[#B0B3B8]">
                    Resend code in <span className="font-semibold text-blue-500">{timeLeft}s</span>
                  </p>
                ) : (
                  <button
                    type="button"
                    onClick={handleResend}
                    className="text-sm font-medium text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors"
                  >
                    Resend verification code
                  </button>
                )}
              </div>

              {/* Verify Button */}
              <button
                type="submit"
                disabled={isLoading}
                className="btn-primary w-full flex justify-center items-center py-2.5 px-4 rounded-xl text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 transition-all shadow-sm hover:shadow-md disabled:opacity-70 disabled:cursor-not-allowed"
              >
                {isLoading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                    </svg>
                    Verifying...
                  </>
                ) : (
                  <>
                    Verify Email
                    <ArrowRightIcon className="w-4 h-4 ml-2" />
                  </>
                )}
              </button>

              {/* Back to Login */}
              <p className="text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
                <Link to="/login" className="text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  Back to login
                </Link>
              </p>
            </form>
          </motion.div>
        </div>
      </div>
    </div>
  )
}

export default OTPVerification
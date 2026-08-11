// src/pages/ResetPassword.tsx
import React, { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useTheme } from '../context/ThemeContext'
import Lottie from 'react-lottie-player'
import resetPasswordAnimation from '../assets/lottie/reset-password.json'
import { 
  LockClosedIcon,
  EyeIcon,
  EyeSlashIcon,
  ArrowRightIcon,
  SunIcon,
  MoonIcon,
  CheckBadgeIcon,
  ExclamationCircleIcon,
  ArrowLeftIcon
} from '@heroicons/react/24/outline'
import toast from 'react-hot-toast'

const ResetPassword = () => {
  const { theme, toggleTheme } = useTheme()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitted, setIsSubmitted] = useState(false)
  const [formData, setFormData] = useState({
    password: '',
    confirmPassword: ''
  })
  const [errors, setErrors] = useState<{
    password?: string
    confirmPassword?: string
  }>({})

  // Check if token exists
  React.useEffect(() => {
    if (!token) {
      toast.error('Invalid or missing reset token')
      navigate('/forgot-password')
    }
  }, [token, navigate])

  const getPasswordStrength = (password: string) => {
    let strength = 0
    if (password.length >= 8) strength++
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) strength++
    if (/\d/.test(password)) strength++
    if (/[^a-zA-Z0-9]/.test(password)) strength++
    return strength
  }

  const passwordStrength = getPasswordStrength(formData.password)
  const strengthLabels = ['Weak', 'Fair', 'Good', 'Strong']
  const strengthColors = ['bg-red-500', 'bg-yellow-500', 'bg-blue-500', 'bg-green-500']

  const validateForm = () => {
    const newErrors: { password?: string; confirmPassword?: string } = {}
    if (!formData.password) {
      newErrors.password = 'Password is required'
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters'
    }
    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match'
    }
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!validateForm()) return

    setIsLoading(true)
    try {
      await new Promise(resolve => setTimeout(resolve, 1500))
      setIsSubmitted(true)
      toast.success('Password reset successfully!')
    } catch (error) {
      toast.error('Failed to reset password. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    if (errors[name as keyof typeof errors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }))
    }
  }

  if (!token) {
    return null // Will redirect via useEffect
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
              animationData={resetPasswordAnimation}
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
              <LockClosedIcon className="w-8 h-8 sm:w-10 sm:h-10 text-white" />
            </div>
          </div>
          <h2 className="mt-4 sm:mt-6 text-center text-2xl sm:text-3xl font-bold text-gray-900 dark:text-white">
            Reset Password
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
            Enter your new password below
          </p>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mt-6 sm:mt-8 bg-white dark:bg-[#242526] py-8 px-6 sm:px-10 shadow-xl rounded-2xl border border-gray-100 dark:border-[#3E4042]"
          >
            {!isSubmitted ? (
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* New Password */}
                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                    New Password
                  </label>
                  <div className="mt-1 relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <LockClosedIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8]" />
                    </div>
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      value={formData.password}
                      onChange={handleChange}
                      className={`app-input block w-full pl-10 pr-10 py-2.5 rounded-xl text-sm ${
                        errors.password ? 'border-red-500 focus:ring-red-500' : ''
                      }`}
                      placeholder="••••••••"
                      autoFocus
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    >
                      {showPassword ? (
                        <EyeSlashIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                      ) : (
                        <EyeIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                      )}
                    </button>
                  </div>
                  {errors.password && (
                    <p className="mt-1 text-sm text-red-500 flex items-center">
                      <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                      {errors.password}
                    </p>
                  )}
                  {/* Password Strength Indicator */}
                  {formData.password && (
                    <div className="mt-2">
                      <div className="flex items-center space-x-2">
                        <div className="flex-1 h-1.5 bg-gray-200 dark:bg-[#3E4042] rounded-full overflow-hidden">
                          <div 
                            className={`h-full ${strengthColors[passwordStrength]} transition-all duration-300`}
                            style={{ width: `${(passwordStrength / 4) * 100}%` }}
                          />
                        </div>
                        <span className="text-xs text-gray-500 dark:text-[#B0B3B8]">
                          {strengthLabels[passwordStrength] || 'Weak'}
                        </span>
                      </div>
                    </div>
                  )}
                </div>

                {/* Confirm Password */}
                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 dark:text-[#E4E6EB]">
                    Confirm Password
                  </label>
                  <div className="mt-1 relative">
                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                      <LockClosedIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8]" />
                    </div>
                    <input
                      id="confirmPassword"
                      name="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      value={formData.confirmPassword}
                      onChange={handleChange}
                      className={`app-input block w-full pl-10 pr-10 py-2.5 rounded-xl text-sm ${
                        errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : ''
                      }`}
                      placeholder="••••••••"
                    />
                    <button
                      type="button"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center"
                    >
                      {showConfirmPassword ? (
                        <EyeSlashIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                      ) : (
                        <EyeIcon className="h-5 w-5 text-gray-400 dark:text-[#B0B3B8] hover:text-gray-600 dark:hover:text-[#E4E6EB] transition-colors" />
                      )}
                    </button>
                  </div>
                  {errors.confirmPassword && (
                    <p className="mt-1 text-sm text-red-500 flex items-center">
                      <ExclamationCircleIcon className="w-4 h-4 mr-1" />
                      {errors.confirmPassword}
                    </p>
                  )}
                </div>

                {/* Submit Button */}
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
                      Resetting...
                    </>
                  ) : (
                    <>
                      Reset Password
                      <ArrowRightIcon className="w-4 h-4 ml-2" />
                    </>
                  )}
                </button>

                {/* Back to Login */}
                <p className="text-center text-sm text-gray-600 dark:text-[#B0B3B8]">
                  <Link to="/login" className="inline-flex items-center text-blue-500 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                    <ArrowLeftIcon className="w-4 h-4 mr-1" />
                    Back to login
                  </Link>
                </p>
              </form>
            ) : (
              // Success State
              <div className="text-center space-y-6">
                <div className="flex justify-center">
                  <div className="w-20 h-20 bg-green-100 dark:bg-green-900/20 rounded-full flex items-center justify-center">
                    <CheckBadgeIcon className="w-10 h-10 text-green-500" />
                  </div>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 dark:text-white">
                  Password Reset Successfully!
                </h3>
                <p className="text-sm text-gray-600 dark:text-[#B0B3B8]">
                  Your password has been reset. You can now login with your new password.
                </p>
                <Link
                  to="/login"
                  className="inline-flex items-center justify-center w-full py-2.5 px-4 rounded-xl text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 transition-all shadow-sm hover:shadow-md"
                >
                  Go to Login
                  <ArrowRightIcon className="w-4 h-4 ml-2" />
                </Link>
              </div>
            )}
          </motion.div>
        </div>
      </div>
    </div>
  )
}

export default ResetPassword
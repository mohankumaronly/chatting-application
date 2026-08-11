// src/App.tsx
import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
// import { ThemeProvider } from './context/ThemeContext'
import { Toaster } from 'react-hot-toast'

// Pages (we'll create these next)
// import Landing from './pages/Landing'
import { ThemeProvider } from './context/ThemeContext'
import Landing from './pages/Landing'
// import Login from './pages/Login'
// import Register from './pages/Register'
// import OTPVerification from './pages/OTPVerification'
// import ProfileSetup from './pages/ProfileSetup'

function App() {
  return (
    <ThemeProvider>
      <div className="min-h-screen bg-background text-foreground">
        <Routes>
          <Route path="/" element={<Landing />} />
          {/* <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/verify-otp" element={<OTPVerification />} />
          <Route path="/setup-profile" element={<ProfileSetup />} /> */}
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
        <Toaster 
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: 'var(--background)',
              color: 'var(--foreground)',
            },
          }}
        />
      </div>
    </ThemeProvider>
  )
}

export default App
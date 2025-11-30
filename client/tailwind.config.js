/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  safelist: [
    'bg-primary-50', 'bg-primary-100', 'bg-primary-200', 'bg-primary-300', 'bg-primary-400',
    'bg-primary-500', 'bg-primary-600', 'bg-primary-700', 'bg-primary-800', 'bg-primary-900',
    'text-primary-50', 'text-primary-100', 'text-primary-200', 'text-primary-300', 'text-primary-400',
    'text-primary-500', 'text-primary-600', 'text-primary-700', 'text-primary-800', 'text-primary-900'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#e6f7ee',
          100: '#c3ecd6',
          200: '#9fe0bc',
          300: '#7bd4a2',
          400: '#57c989',
          500: '#33be70',
          600: '#2e9c5c',
          700: '#297a49',
          800: '#235836',
          900: '#1e3623',
        },
      },
    },
  },
  plugins: [],
}
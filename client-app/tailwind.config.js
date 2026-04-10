/** @type {import('tailwindcss').Config} */

export default {
    content: ['./index.html', './src/**/*.{js,jsx}'],
    theme: {
        extend: {
            colors: {
                ntu: {
                    rose: '#d30253',
                    pink: '#d30253',
                    tundora: '#404040',
                    dark: '#333333',
                },
            },
        },
    },
    plugins: [],
};
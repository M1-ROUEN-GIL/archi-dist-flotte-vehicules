import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import { AuthProvider } from '@flotte/shared-auth'
import {AppGraphQLProvider} from "@flotte/shared-client/src/GraphQLProvider.tsx";

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <AuthProvider>
            <AppGraphQLProvider>
                <App />
            </AppGraphQLProvider>
        </AuthProvider>
    </React.StrictMode>,
)
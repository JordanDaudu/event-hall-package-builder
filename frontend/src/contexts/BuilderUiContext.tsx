import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";

interface BuilderUiContextValue {
    currentStep: number;
    setCurrentStep: (n: number) => void;
    runningTotal: number;
    setRunningTotal: (n: number) => void;
    isBuilderActive: boolean;
    setBuilderActive: (v: boolean) => void;
}

const BuilderUiContext = createContext<BuilderUiContextValue>({
    currentStep: 0,
    setCurrentStep: () => {},
    runningTotal: 0,
    setRunningTotal: () => {},
    isBuilderActive: false,
    setBuilderActive: () => {},
});

export function BuilderUiProvider({ children }: { children: ReactNode }) {
    const [currentStep, setCurrentStep] = useState(0);
    const [runningTotal, setRunningTotal] = useState(0);
    const [isBuilderActive, setBuilderActive] = useState(false);

    return (
        <BuilderUiContext.Provider
            value={{ currentStep, setCurrentStep, runningTotal, setRunningTotal, isBuilderActive, setBuilderActive }}
        >
            {children}
        </BuilderUiContext.Provider>
    );
}

export function useBuilderUi() {
    return useContext(BuilderUiContext);
}

import { createPortal } from "react-dom";
import type { ReactNode } from "react";

interface ModalProps {
    open: boolean;
    title: string;
    onClose: () => void;
    children: ReactNode;
    width?: number;
}

export default function Modal({ open, title, onClose, children, width = 540 }: ModalProps) {
    if (!open) return null;
    return createPortal(
        <div className="modal-overlay" onClick={onClose}>
            <div
                className="modal-dialog"
                style={{ maxWidth: width }}
                onClick={(e) => e.stopPropagation()}
                role="dialog"
                aria-modal="true"
            >
                <div className="modal-header">
                    <h2 className="modal-title">{title}</h2>
                    <button className="modal-close" onClick={onClose} aria-label="סגור">✕</button>
                </div>
                <div className="modal-body">{children}</div>
            </div>
        </div>,
        document.body
    );
}

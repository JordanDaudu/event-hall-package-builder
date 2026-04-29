import Modal from "./Modal";

interface ConfirmDeleteDialogProps {
    open: boolean;
    title?: string;
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
    loading?: boolean;
}

export default function ConfirmDeleteDialog({
    open,
    title = "אישור מחיקה",
    message,
    onConfirm,
    onCancel,
    loading = false,
}: ConfirmDeleteDialogProps) {
    return (
        <Modal open={open} title={title} onClose={onCancel} width={420}>
            <p style={{ margin: "0 0 24px", color: "var(--color-ink-soft)", lineHeight: 1.6 }}>
                {message}
            </p>
            <div className="modal-actions">
                <button className="button button-secondary" onClick={onCancel} disabled={loading}>
                    ביטול
                </button>
                <button className="button button-danger" onClick={onConfirm} disabled={loading}>
                    {loading ? "מוחק..." : "מחיקה"}
                </button>
            </div>
        </Modal>
    );
}

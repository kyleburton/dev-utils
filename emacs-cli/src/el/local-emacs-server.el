(defun krb-get-emacs-pwd ()
  (let ((root-dirname nil))
    (with-current-buffer "*scratch*"
      (setq root-dirname default-directory))
    root-dirname))

(defvar *krb-emacs-server-file* (format "%s.emacs-server"
                                        (krb-get-emacs-pwd)))

(defun krb-boostrap-local-eamcs-server ()
  (if (not (file-exists-p *krb-emacs-server-file*))
      (make-directory *krb-emacs-server-file*))
  (set-file-modes *krb-emacs-server-file* #o700)
  (setq server-socket-dir *krb-emacs-server-file*)
  (autoload 'server-running-p "server")
  (unless
      (server-running-p)
    (server-start)))

(krb-boostrap-local-eamcs-server)



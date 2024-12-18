import styles from './WorkspaceMenu.module.css'

export function WorkspaceMenu({children}) {
  return(
    <div className={styles.container}>
      {children}
    </div>
  );
}
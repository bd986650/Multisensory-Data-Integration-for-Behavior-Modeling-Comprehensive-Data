import styles from './WorkspaceMap.module.css'

export function WorkspaceMap({children}) {
  return(
    <div className={styles.container}>
      {children}
    </div>
  );
}
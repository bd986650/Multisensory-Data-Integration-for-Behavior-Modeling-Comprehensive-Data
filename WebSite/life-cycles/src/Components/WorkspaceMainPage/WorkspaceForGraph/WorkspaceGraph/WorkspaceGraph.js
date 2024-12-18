import styles from './WorkspaceGraph.module.css'

export function WorkspaceGraph({children}) {
  return(
    <div className={styles.container}>
      {children}
    </div>
  );
}
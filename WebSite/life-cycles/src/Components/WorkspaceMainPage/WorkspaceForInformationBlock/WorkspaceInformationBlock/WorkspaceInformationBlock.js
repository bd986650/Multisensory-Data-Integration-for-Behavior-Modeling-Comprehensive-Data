import styles from './WorkspaceInformationBlock.module.css'

export function WorkspaceInformationBlock({children}) {
  return(
    <div className={styles.container}>
      {children}
    </div>
  );
}
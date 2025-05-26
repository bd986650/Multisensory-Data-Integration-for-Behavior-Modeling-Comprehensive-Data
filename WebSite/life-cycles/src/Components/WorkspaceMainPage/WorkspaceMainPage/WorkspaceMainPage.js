import { Timeline } from '../TimeLine/TimeLine';
import { InformationBlock } from '../WorkspaceForInformationBlock/InformationBlock/InformationBlock';
import { WorkspaceInformationBlock } from '../WorkspaceForInformationBlock/WorkspaceInformationBlock/WorkspaceInformationBlock';
import { ButtonRequestData } from '../WorkspaceForMap/ButtonRequestData/ButtonRequestData';
import { Map } from '../WorkspaceForMap/Map/Map';
import { WorkspaceMap } from '../WorkspaceForMap/WorkspaceMap/WorkspaceMap';
import styles from './WorkspaceMainPage.module.css';

export function WorkspaceMainPage({ currentTime, setCurrentTime }) {  // Добавили currentTime как пропс
  return (
    <div className={styles.container}>
      <WorkspaceMap>
        <Timeline onTimeChange={setCurrentTime} />
        <Map/>
        <ButtonRequestData />
      </WorkspaceMap>
      <WorkspaceInformationBlock>
        <InformationBlock />
      </WorkspaceInformationBlock>
    </div>
  );
}

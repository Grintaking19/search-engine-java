import styles from './mainSearchPage.module.css';
import SearchIcon from '@material-ui/icons/Search';

export default function MainSearchPage() {



  return (
    <div className={styles["main-search-page"]}>
      <div className={styles["main-search-page--container"]}>
        <h1 className={styles["main-search-page--title"]}>Wiki Link</h1>
        <div className={styles["search-bar-container"]}>
          <input className={styles["search-bar"]} type="text" />
          <div className={styles["search-icon"]}>
            <SearchIcon />
          </div>
        </div>
      </div>


    </div>
  )
}
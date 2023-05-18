import styles from './mainSearchPage.module.css';
import SearchIcon from '@material-ui/icons/Search';
import CloseIcon from '@material-ui/icons/Close';
import suggestions from './data-suggest.json'
import { useEffect, useState } from 'react'
import ProtoTypes from 'prop-types';
import axios from 'axios';

export default function MainSearchPage({ setSearch, setEnableSearch, search }) {

  const [suggestionFilter, setSuggestionFilter] = useState([]);
  const [page, setPage] = useState(1);
  const [searchResult, setSearchResult] = useState([]);
  const [searchResultCount, setSearchResultCount] = useState(0);
  const [searchResultTime, setSearchResultTime] = useState(0);
  

  const handleFilter = (event) => {
    let filteredSuggestions = suggestions.words.filter((word) => {
      return word.toLowerCase().includes(event.target.value.toLowerCase())
    })

    // if (filteredSuggestions.length === 0) {

    // }

    if (event.target.value === '') {
      filteredSuggestions = [];
    }
    setSuggestionFilter(filteredSuggestions)
  }

  const handleEnter = (event) => {
    if (event.key === 'Enter') {
      console.log(event.target.value);
      handleSearch(event.target.value);
    }
  }

  const handleSearch = async (word) => {
    console.log(word);
    setSearch(word);
    setEnableSearch(true);
    let now = new Date();
    const response = await axios.post(`http://localhost:8081/search?page=${page}&&pageSize=20`, { "query": search });
    setSearchResult(response.pages);
    setSearchResultCount(response.total_number);
    //setSearchResultTime(0.1);
    let later = new Date();
    setSearchResultTime((later - now));
  }

  const close = () => {
    setSuggestionFilter([]);
  }

  // This is for the first time load or refresh page or go back to this page
  useEffect(() => {
    setEnableSearch(false);
  }, [])

  return (
    <div className={styles["main-search-page"]}>
      <div className={styles["main-search-page--container"]}>
        <h1 className={styles["main-search-page--title"]} >Wiki Link</h1>
        <div className={styles["search-bar-container"]}>
          <input className={styles["search-bar"]} type="text" onChange={handleFilter} onKeyDown={handleEnter} />
          <div className={styles["search-icon"]}>
            {suggestionFilter.length > 0 ? <CloseIcon onClick={() => { close() }} /> : <SearchIcon />}
          </div>
        </div>
        <div className={styles['suggestions-container']}>
          {
            suggestionFilter.slice(0, 10).map((word, index) => {
              return (
                <div key={index} className={styles["suggestion"]} onClick={() => { handleSearch(word) }}>
                  <p className={styles["suggestion--content"]}>{word}</p>
                </div>
              )
            })
          }

        </div>

      </div>


    </div>
  )
}


MainSearchPage.propTypes = {
  setSearch: ProtoTypes.func.isRequired,
  setEnableSearch: ProtoTypes.func.isRequired
}
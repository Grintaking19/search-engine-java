import PropTypes from 'prop-types'
import styles from './SearchResultPage.module.css'
import SearchIcon from '@material-ui/icons/Search'
import CloseIcon from '@material-ui/icons/Close'
import suggestions from './data-suggest.json'
import { useEffect, useState } from 'react'
import axios from 'axios'

export function SearchResultPage({ search, setSearch, setEnableSearch }) {
  const [searchResult, setSearchResult] = useState([]);
  const [searchResultCount, setSearchResultCount] = useState(0);
  const [searchResultTime, setSearchResultTime] = useState(0);
  const [inputValue, setInputValue] = useState(search);
  const [suggestionFilter, setSuggestionFilter] = useState([]);
  const [page, setPage] = useState(1);
  // This is for the first time load or refresh page or go back to this page



  const handleFilter = (event) => {
    setInputValue(event.target.value);
    let filteredSuggestions = suggestions.words.filter((word) => {
      return word.toLowerCase().includes(event.target.value.toLowerCase())
    })

    // if (filteredSuggestions.length === 0) {

    // }



    useEffect(() => {
      async function fetchData() {
        try {
          let now = new Date();
          const response = await axios.post(`http://localhost:8081/search?page=${page}&&pageSize=20`, search);
          setSearchResult(response.pages);
          setSearchResultCount(response.total_number);
          //setSearchResultTime(0.1);
          let later = new Date();
          setSearchResultTime((later - now));
        }
        catch (err) {
          console.log(err);
        }
      };
      fetchData();

    }, [search])

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

  const handleSearch = (word) => {
    console.log(word);
    setSearch(word);
    setEnableSearch(true);
  }

  const close = () => {
    setInputValue('');
    setSuggestionFilter([]);
  }


  return (
    <div className={styles["search-result-page"]}>
      <div className={styles['nav-bar']}>
        <h1 className={styles["main-search-page--title"]} onClick={() => { setEnableSearch(false) }}>Wiki Links</h1>
        <div className={styles["search-bar-container"]}>
          <input className={styles["search-bar"]} type="text" onChange={handleFilter} onKeyDown={handleEnter} value={inputValue} />
          <div className={styles["search-icon"]}>
            {search.length > 0 ? <CloseIcon onClick={() => close()} /> : <SearchIcon />}
          </div>
        </div>
      </div>
      <div className={styles['page-content']}>
        <div className={styles["page-space-left"]}>
        </div>

        <div className={styles['search-result-container']}>
          <p className={styles['search-result-count']}>About {searchResultCount} results ({searchResultTime} seconds)</p>
          <div className={styles['search-result']}>
            {searchResult.map((result, index) => {
              return (
                <div key={index} className={styles['search-result--item']}>
                  {
                    searchResult.map((result, index) => {
                      return (
                        <div key={index} className={styles['search-result--item-container']}>
                          <a href={result.url} className={styles['search-result--item-title']}>{result.title}</a>
                          <p className={styles['search-result--item-content']}>{result.discription}</p>
                        </div>
                      )
                    })
                  }
                </div>
              )
            })}
          </div>
        </div>

        <div className={styles["page-space-right"]}>
        </div>
      </div>
    </div>
  )
}


import PropTypes from 'prop-types'
import styles from './SearchResultPage.module.css'
import SearchIcon from '@material-ui/icons/Search'
import CloseIcon from '@material-ui/icons/Close'
import suggestions from './data-suggest.json'
import { useEffect, useState } from 'react'
import axios from 'axios'

export function SearchResultPage({ search, setSearch, setEnableSearch, setSearchResult, setSearchResultCount, setSearchResultTime, searchResult, searchResultCount,
  searchResultTime, page }) {
  const [inputValue, setInputValue] = useState(search);
  const [suggestionFilter, setSuggestionFilter] = useState([]);
  // This is for the first time load or refresh page or go back to this page



  const handleFilter = (event) => {
    setInputValue(event.target.value);
    let filteredSuggestions = suggestions.words.filter((word) => {
      return word.toLowerCase().includes(event.target.value.toLowerCase())
    })

    // if (filteredSuggestions.length === 0) {

    // }



    // useEffect(() => {
    //   async function fetchData() {


    //   }
    //   onsole.log("Fetching data...");
    //   fetchData();

    // }, [])

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
    try {
      let now = new Date();
      setSearch(word);
      console.log("The is the search word of main search page: " + word);
      const response = await axios.post(`http://localhost:8081/search?page=${page}&&pageSize=20`, { "query": search });
      setSearchResult(response.pages);
      setSearchResultCount(response.total_number);

      let later = new Date();
      setSearchResultTime((later - now));
      setEnableSearch(true);
    } catch (error) {
      console.log(error);
    }
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


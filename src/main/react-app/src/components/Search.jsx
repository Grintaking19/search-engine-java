import { useState } from 'react';
import MainSearchPage from './MainSearchPage'
import { SearchResultPage } from './SearchResultPage';

export default function Search() {

  const [search, setSearch] = useState('');
  const [enableSearch, setEnableSearch] = useState(false);
  const [searchResult, setSearchResult] = useState([]);
  const [searchResultCount, setSearchResultCount] = useState(0);
  const [searchResultTime, setSearchResultTime] = useState(0);
  const [page, setPage] = useState(1);
  //setSearchResult, setSearchResultCount, setSearchResultTime , searchResult, searchResultCount, searchResultTime
  console.log("search");
  return (
    <>
      {
        !enableSearch ? (
          <MainSearchPage setSearch={setSearch} setEnableSearch={setEnableSearch} search={search} setSearchResult={setSearchResult}
            setSearchResultCount={setSearchResultCount} setSearchResultTime={setSearchResultTime}
            page={page} />
        )
          :
          (
            //setSearch, setEnableSearch 
            <SearchResultPage search={search} setSearch={setSearch} setEnableSearch={setEnableSearch}
              setSearchResult={setSearchResult} setSearchResultCount={setSearchResultCount} setSearchResultTime={setSearchResultTime}
              searchResult={searchResult} searchResultCount={searchResultCount} searchResultTime={searchResultTime}
              page={page} />
          )
      }


    </>
  )

}
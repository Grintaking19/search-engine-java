import { useState } from 'react';
import MainSearchPage from './MainSearchPage'
import { SearchResultPage } from './SearchResultPage';

export default function Search() {

  const [search, setSearch] = useState('');
  const [enableSearch, setEnableSearch] = useState(false);

  console.log("search");
  return (
    <>
      {
        !enableSearch ? (
          <MainSearchPage setSearch={setSearch} setEnableSearch={setEnableSearch} search={search} />
        )
          :
          (
            //setSearch, setEnableSearch 
            <SearchResultPage search={search} setSearch={setSearch} setEnableSearch={setEnableSearch} />
          )
      }


    </>
  )

}